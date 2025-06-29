import React, { useContext} from 'react';
import styles from './style.module.css';
import {BookType} from "../../types";
import { AppContext } from "../../context";
import { MiniButton } from "../MiniButton/MiniButton";
import { BookForm } from "../BookForm/BookForm";
import {Warning} from "../Warning/Warning";
import {useStateWithUpdater} from "../../hooks/useStateWithUpdater";

type BookProps =  BookType & {
    onDelete: (bookName: string) => Promise<void>;
    onUpdate: (bookName: string, updatedBook: BookType) => Promise<void>;
}

type BookState = {
    showDetails: boolean,
    showEditForm: boolean,
    warning: boolean,
    error: string
    loading: boolean
};

const initialState: BookState = {
    showDetails: false,
    showEditForm: false,
    loading: false,
    error: '',
    warning: false
}

export const Book: React.FC<BookProps> = ({ onDelete, onUpdate, ...book }) => {
    const context = useContext(AppContext);
    const [state, updateState] = useStateWithUpdater<BookState>(initialState);

    const handleSubmitEdit = async (updatedBook: BookType) => {
        console.log('📝 Book: handleSubmitEdit called with:', updatedBook);
        try {
            updateState({loading: true, error: ''})
            await onUpdate(book.name, updatedBook);
            updateState({loading: false,showEditForm: false})
        } catch (error) {
            console.error('Failed to update book:', error);
            updateState({loading: false, error: 'Could not update book! Please try again.'})
        }
    };

    const handleDeleteBook = async () => {
        try {
            updateState({loading: true})
            console.log('🗑️ Book: Requesting deletion...', book.name);
            await onDelete(book.name);
        } catch (error) {
            console.error('❌ Book: Failed to delete book:', error);
            alert('This book cannot be deleted!');
        }finally {
            updateState({loading: false})
        }
    };

    return (
        <>
            {state.warning && (
                <Warning
                    onClick={async ()=>{
                        updateState({warning: false});
                        await handleDeleteBook();
                    }}
                    onCancel={() => updateState({warning: false})}
                    purpose='delete'
                    message={'Are you sure about deleting this book?'}
                />
            )}
            <div className={styles.bookContainer}>
                <div className={styles.bookInfo}>
                    <h3 className={styles.bookName} onClick={() => updateState({showDetails: !state.showDetails})}>
                        {book.name}
                    </h3>
                    <p className={styles.bookAuthor}>by {book.author}</p>
                </div>
                <div className={styles.bookActions}>
                    <p className={styles.bookPrice}>${book.price}</p>
                    <p className={styles.bookQuantity}>
                        {context.checkQuantity(book.name)} {context.checkQuantity(book.name) === 1 ? 'item' : 'items'}
                    </p>
                    {context?.role === 'CLIENT' ? (
                        <div className={styles.employeeActions}>
                            <MiniButton topic='basket' size='medium' onClick={() => context.addToBasket(book.name)}/>
                            <MiniButton topic='bin' size='medium' onClick={() => context.removeFromBasket(book.name)}/>
                        </div>
                    ) : (
                        <div className={styles.employeeActions}>
                            <MiniButton topic='bin' size='medium' onClick={() => updateState({warning: true})}/>
                            <MiniButton topic='update' size='medium' onClick={() => updateState({showEditForm: true})}/>
                        </div>
                    )}
                </div>
            </div>

            {state.showDetails && (
                <div className={styles.overlay} onClick={() => updateState({showDetails: !state.showDetails})}>
                    <article className={styles.bookDescription} onClick={(e) => e.stopPropagation()}>
                        <div className={styles.bookDetailsHeader}>
                            <h3>{book.name}</h3>
                            <MiniButton topic='cross' size='premedium' onClick={() => updateState({showDetails: !state.showDetails})}/>
                        </div>
                        <div className={styles.bookDetailsContent}>
                            <p>This book was written by <strong>{book.author}</strong> in <strong>{book.publication_date}</strong>.</p>
                            <p><strong>Genre</strong>: {book.genre}</p>
                            <p><strong>Age group</strong>: {book.age_group}</p>
                            <p><strong>Language</strong>: {book.language}</p>
                            <p><strong>Pages</strong>: {book.pages}</p>
                            <p><strong>Price</strong>: ${book.price}</p>
                            <p><strong>Characteristics</strong>: {book.characteristics}</p>
                            <p><strong>Description</strong>: {book.description}</p>
                        </div>
                    </article>
                </div>
            )}
            {state.showEditForm && (
                <BookForm
                    initialData={book}
                    onSubmit={handleSubmitEdit}
                    onCancel={()=>updateState({loading: false, showEditForm: false, error: ''})}
                    error={state.error}
                    processing={state.loading}
                />
            )}
        </>
    );
};