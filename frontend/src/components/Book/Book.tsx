import React, {useContext, useState} from 'react';
import styles from './style.module.css';
import {BookType} from "../../types";
import {AppContext} from "../../context";
import {MiniButton} from "../MiniButton/MiniButton";

export const Book: React.FC<BookType> = (book) => {
    const context = useContext(AppContext);
    const [showDetails, setShowDetails] = useState(false);

    const toggleDetails = () => {
        setShowDetails(!showDetails);
    };

    return (
        <div className={styles.bookContainer}>
            <p
                className={styles.bookName}
                onClick={toggleDetails}
                style={{ cursor: 'pointer', color: 'blue', textDecoration: 'underline' }}
            >
                {book.name}
            </p>

            {showDetails && (
                <article className={styles.bookDescription}>
                    <div className={styles.bookDetailsHeader}>
                        <h2>{book.name}</h2>
                        <MiniButton
                            topic='cross'
                            size='mini'
                            onClick={toggleDetails}
                        />
                    </div>
                    <p>This book was written by <strong>{book.author}</strong> in <strong>{book.publication_date}</strong>.</p>
                    <p><strong>Genre</strong>: {book.genre}</p>
                    <p><strong>Age group</strong>: {book.age_group}</p>
                    <p><strong>Language</strong>: {book.language}</p>
                    <p><strong>Pages</strong>: {book.pages}</p>
                    <p><strong>Price</strong>: ${book.price}</p>
                    <p><strong>Characteristics</strong>: {book.characteristics}</p>
                    <p><strong>Description</strong>: {book.description}</p>
                </article>
            )}
            <h2>{book.name}</h2>
            <p className={styles.bookPrice}>${book.price}</p>

            <MiniButton
                topic='basket'
                size='medium'
                onClick={() => context.addToBasket({bookName: book.name, quantity: 1})}
            />
        </div>
    );
};