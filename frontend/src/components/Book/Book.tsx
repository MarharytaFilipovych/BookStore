import React, { useContext, useState } from 'react';
import styles from './style.module.css';
import { BookType } from "../../types";
import { AppContext } from "../../context";
import { MiniButton } from "../MiniButton/MiniButton";
import { BookForm } from "../BookForm/BookForm";
import {Warning} from "../Warning/Warning";

interface BookProps extends BookType {
    onDelete: (bookName: string) => Promise<void>;
    onUpdate: (bookName: string, updatedBook: BookType) => Promise<void>;
}

export const Book: React.FC<BookProps> = ({ onDelete, onUpdate, ...book }) => {
    const context = useContext(AppContext);
    const [showDetails, setShowDetails] = useState(false);
    const [showEditForm, setShowEditForm] = useState(false);
    const [isProcessing, setIsProcessing] = useState(false);
    const [formError, setFormError] = useState<string>('');
    const [warning, setWarning] = useState<boolean>(false);

    const handleSubmitEdit = async (updatedBook: BookType) => {
        console.log('📝 Book: handleSubmitEdit called with:', updatedBook);
        try {
            setIsProcessing(true);
            setFormError('');
            await onUpdate(book.name, updatedBook);
            setShowEditForm(false);
            setIsProcessing(false);

        } catch (error) {
            console.error('Failed to update book:', error);
            setFormError('Could not update book! Please try again.');
            setIsProcessing(false);
        }
    };

    const handleDeleteBook = async () => {
        try {
            setIsProcessing(true);
            console.log('🗑️ Book: Requesting deletion...', book.name);
            await onDelete(book.name);
        } catch (error) {
            console.error('❌ Book: Failed to delete book:', error);
            setIsProcessing(false);
            alert('Failed to delete book. Please try again.');
        }
    };

    const handleCancelEdit = () => {
        setShowEditForm(false);
        setFormError('');
        setIsProcessing(false);
    };

    return (
        <>
            {warning && (
                <Warning
                    onClick={async ()=>{
                        setWarning(false);
                        await handleDeleteBook();
                    }}
                    onCancel={() => setWarning(false)}
                    purpose='delete'
                    message={'Are you sure about deleting this book?'}
                />
            )}
            <div className={styles.bookContainer}>
                <div className={styles.bookInfo}>
                    <h3 className={styles.bookName} onClick={() => setShowDetails(!showDetails)}>
                        {book.name}
                    </h3>
                    <p className={styles.bookAuthor}>by {book.author}</p>
                </div>

                <div className={styles.bookActions}>
                    <p className={styles.bookPrice}>${book.price}</p>
                    {context?.role === 'CLIENT' ? (
                        <MiniButton
                            topic='basket'
                            size='medium'
                            onClick={() => context.addToBasket({book_name: book.name, quantity: 1})}
                        />
                    ) : (
                        <div className={styles.employeeActions}>
                            <MiniButton
                                topic='bin'
                                size='medium'
                                onClick={() => setWarning(true)}
                            />
                            <MiniButton
                                topic='update'
                                size='medium'
                                onClick={() => setShowEditForm(true)}
                            />
                        </div>
                    )}
                </div>
            </div>

            {showDetails && (
                <div className={styles.overlay} onClick={() => setShowDetails(!showDetails)}>
                    <article className={styles.bookDescription} onClick={(e) => e.stopPropagation()}>
                        <div className={styles.bookDetailsHeader}>
                            <h3>{book.name}</h3>
                            <MiniButton
                                topic='cross'
                                size='premedium'
                                onClick={() => setShowDetails(!showDetails)}
                            />
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

            {showEditForm && (
                <BookForm
                    initialData={book}
                    onSubmit={handleSubmitEdit}
                    onCancel={handleCancelEdit}
                    error={formError}
                    processing={isProcessing}
                />
            )}
        </>
    );
};