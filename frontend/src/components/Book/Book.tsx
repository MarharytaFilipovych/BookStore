import React, { useContext, useState } from 'react';
import styles from './style.module.css';
import { BookType } from "../../types";
import { AppContext } from "../../context";
import { MiniButton } from "../MiniButton/MiniButton";
import { BookService } from "../../services/BookService";
import { BookForm } from "../BookForm/BookForm";

export const Book: React.FC<BookType> = (book) => {
    const context = useContext(AppContext);
    const [showDetails, setShowDetails] = useState(false);
    const [showEditForm, setShowEditForm] = useState(false);

    // Add state for BookForm props
    const [isProcessing, setIsProcessing] = useState(false);
    const [formError, setFormError] = useState<string>('');

    const handleSubmitEdit = async (updatedBook: BookType) => {
        try {
            setIsProcessing(true);
            setFormError('');

            await BookService.updateBook(book.name, updatedBook);

            setShowEditForm(false);
            setIsProcessing(false);
             window.location.reload();

        } catch (error) {
            console.error('Failed to update book:', error);
            setFormError('Failed to update book. Please try again.');
            setIsProcessing(false);
        }
    };

    const handleDeleteBook = async () => {
        // Optional: Add confirmation dialog
        if (!window.confirm(`Are you sure you want to delete "${book.name}"?`)) {
            return;
        }

        try {
            await BookService.deleteBook(book.name);

            // Optional: You might want to refresh the page or update local state
            // window.location.reload(); // Simple refresh

        } catch (error) {
            console.error('Failed to delete book:', error);
            // Optional: Show error notification
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
                            onClick={() => context.addToBasket({bookName: book.name, quantity: 1})}
                        />
                    ) : (
                        <div className={styles.employeeActions}>
                            <MiniButton
                                topic='bin'
                                size='medium'
                                onClick={handleDeleteBook}
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
                            <h2>{book.name}</h2>
                            <MiniButton
                                topic='cross'
                                size='mini'
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