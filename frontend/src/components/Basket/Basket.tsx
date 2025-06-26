import React, { useContext, useState, useEffect } from 'react';
import styles from './style.module.css';
import { AppContext } from "../../context";
import { MiniButton } from "../MiniButton/MiniButton";
import {  BookType } from "../../types";
import { BookService } from "../../services/BookService";

type BasketProps = {
    onClose: () => void;
    onOrder: () => void;
};

export const Basket: React.FC<BasketProps> = ({ onClose, onOrder }) => {
    const context = useContext(AppContext);
    const [bookPrices, setBookPrices] = useState<Record<string, number>>({});
    const [bookDetails, setBookDetails] = useState<Record<string, BookType>>({});
    const [loading, setLoading] = useState(false);
    const [fetchingPrices, setFetchingPrices] = useState(false);

    const fetchBookDetails = async (bookName: string): Promise<{ price: number; book: BookType }> => {
        try {
            const book = await BookService.getBookByName(bookName);
            return { price: book.price, book };
        } catch (error) {
            console.error(`Failed to fetch details for ${bookName}:`, error);
            return {
                price: 0,
                book: {
                    name: bookName,
                    author: 'Unknown',
                    genre: 'Unknown',
                    age_group: 'ADULT',
                    price: 0,
                    publication_date: '',
                    pages: 0,
                    characteristics: '',
                    description: '',
                    language: 'ENGLISH'
                }
            };
        }
    };

    const loadBookDetails = async () => {
        if (context.basket.length === 0) {
            setBookPrices({});
            setBookDetails({});
            return;
        }

        setFetchingPrices(true);
        const prices: Record<string, number> = {};
        const details: Record<string, BookType> = {};

        try {
            const fetchPromises = context.basket
                .filter(item => !bookPrices[item.bookName])
                .map(async (item) => {
                    const { price, book } = await fetchBookDetails(item.bookName);
                    prices[item.bookName] = price;
                    details[item.bookName] = book;
                });

            await Promise.all(fetchPromises);

            setBookPrices(prev => ({ ...prev, ...prices }));
            setBookDetails(prev => ({ ...prev, ...details }));
        } catch (error) {
            console.error('Failed to load book details:', error);
        } finally {
            setFetchingPrices(false);
        }
    };

    useEffect(() => {
        loadBookDetails();
    }, [context.basket]);

    const updateQuantity = (bookName: string, newQuantity: number) => {
        if (newQuantity <= 0) {
            context.removeFromBasket(bookName);
        } else {
            context.addToBasket({ bookName, quantity: newQuantity });
        }
    };

    const changeQuantity = (bookName: string, increase: boolean) => {
        const item = context.basket.find(item => item.bookName === bookName);
        if (item) {
            if(increase)updateQuantity(bookName, item.quantity + 1);
            else updateQuantity(bookName, item.quantity - 1);
        }
    };

    const calculateTotal = (): number => {
        return context.basket.reduce((total, item) => {
            const price = bookPrices[item.bookName] || 0;
            return total + (price * item.quantity);
        }, 0);
    };

    const getTotalItems = (): number => {
        return context.basket.reduce((total, item) => total + item.quantity, 0);
    };

    const handleOrder = async () => {
        if (context.basket.length === 0) return;

        setLoading(true);
        try {
            await onOrder();
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className={styles.overlay} onClick={onClose}>
            <div className={styles.basketContainer} onClick={(e) => e.stopPropagation()}>
                <div className={styles.basketHeader}>
                    <div className={styles.headerContent}>
                        <h2>Your basket!</h2>
                        <div className={styles.basketSummary}>
                            <span className={styles.itemCount}>
                                {getTotalItems()} {getTotalItems() === 1 ? 'item' : 'items'}
                            </span>
                        </div>
                    </div>
                    <MiniButton topic='cross' size='mini' onClick={onClose}/>
                </div>

                <div className={styles.basketContent}>
                    {context.basket.length === 0 ? (
                        <div className={styles.emptyBasket}>
                            <h3>Your basket is empty</h3>
                            <p>Add some books to get started!</p>
                        </div>
                    ) : (
                        <>
                            <div className={styles.basketItems}>
                                {fetchingPrices && (
                                    <div className={styles.loadingIndicator}>
                                        <span>Loading prices...</span>
                                    </div>
                                )}
                                {context.basket.map((item, index) => {
                                    const book = bookDetails[item.bookName];
                                    const price = bookPrices[item.bookName] || 0;
                                    return (
                                        <div key={`${item.bookName}-${index}`} className={styles.basketItem}>
                                            <div className={styles.itemInfo}>
                                                <h4 className={styles.bookName}>{item.bookName}</h4>
                                                {book && (
                                                    <div className={styles.bookMeta}>
                                                        <span className={styles.author}>by {book.author}</span>
                                                        <span className={styles.genre}>{book.genre}</span>
                                                    </div>
                                                )}
                                                <div className={styles.priceInfo}>
                                                    <span className={styles.unitPrice}>
                                                        ${price.toFixed(2)} each
                                                    </span>
                                                    <span className={styles.totalPrice}>
                                                        ${(price * item.quantity).toFixed(2)}
                                                    </span>
                                                </div>
                                            </div>

                                            <div className={styles.itemControls}>
                                                <div className={styles.quantityControls}>
                                                    <button
                                                        className={styles.quantityBtn}
                                                        onClick={() => changeQuantity(item.bookName, false)}
                                                        disabled={item.quantity <= 1}
                                                    >
                                                        -
                                                    </button>
                                                    <span className={styles.quantity}>{item.quantity}</span>
                                                    <button
                                                        className={styles.quantityBtn}
                                                        onClick={() => changeQuantity(item.bookName, true)}
                                                    >
                                                        +
                                                    </button>
                                                </div>
                                                <MiniButton topic='cross' size='mini'
                                                    onClick={() => context.removeFromBasket(item.bookName)}
                                                />
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>

                            <div className={styles.basketFooter}>
                                <div className={styles.totalSection}>
                                    <div className={styles.totalRow}>
                                        <span className={styles.totalLabel}>Subtotal:</span>
                                        <span className={styles.totalAmount}>${calculateTotal().toFixed(2)}</span>
                                    </div>
                                    <div className={styles.totalRow}>
                                        <span className={styles.totalLabel}>Tax:</span>
                                        <span className={styles.totalAmount}>$0.00</span>
                                    </div>
                                    <div className={`${styles.totalRow} ${styles.grandTotal}`}>
                                        <span className={styles.totalLabel}>Total:</span>
                                        <span className={styles.totalAmount}>${calculateTotal().toFixed(2)}</span>
                                    </div>
                                </div>

                                <div className={styles.actionButtons}>
                                    <button
                                        className={styles.clearButton}
                                        onClick={context.clearBasket}
                                        disabled={loading}
                                    >
                                        Clear Basket
                                    </button>
                                    <button
                                        className={styles.orderButton}
                                        onClick={handleOrder}
                                        disabled={loading || context.basket.length === 0}
                                    >
                                        {loading ? 'Processing...' : 'Place Order'}
                                    </button>
                                </div>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};