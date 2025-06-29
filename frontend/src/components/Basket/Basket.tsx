import React, { useContext, useEffect } from 'react';
import styles from './style.module.css';
import { AppContext } from "../../context";
import { MiniButton } from "../MiniButton/MiniButton";
import {BookType, State} from "../../types";
import { BookService } from "../../services/BookService";
import {useStateWithUpdater} from "../../hooks/useStateWithUpdater";
import {OrderService} from "../../services/OrderService";
import {Icon} from "../Icon/Icon";
import {ActionButton} from "../AuthorizationButton/ActionButton";

type BasketState = {
    bookPrices: Record<string, number>;
    bookDetails: Record<string, BookType>;
} & State;

const initialState: BasketState = {
    loading: false,
    error: false,
    bookDetails: {},
    bookPrices: {}
}
export const Basket: React.FC<{onClose: () => void}> = ({ onClose }) => {
    const context = useContext(AppContext);
    const [state, updateState] = useStateWithUpdater<BasketState>(initialState);

    const handleOrder = async () => {
        if (context.basket.length === 0) return;

        updateState({loading: true});
        try {
            const orderData = {
                client_email: context.user?.email || '',
                employee_email: undefined,
                order_date: new Date().toISOString(),
                price: calculateTotal(),
                book_items: context.basket.map(item => ({
                    book_name: item.book_name,
                    quantity: item.quantity
                }))
            };
            await OrderService.createOrder(orderData);
            context.clearBasket();
            onClose();
        } catch (error) {
            console.error('Failed to place order:', error);
            updateState({ error: true });
        } finally {
            updateState({loading: false});
        }
    };

    const loadBookDetails = async () => {
        if (context.basket.length === 0) {
            updateState({bookPrices: {}, bookDetails: {}});
            return;
        }

        updateState({loading: true});
        const prices: Record<string, number> = {};
        const details: Record<string, BookType> = {};

        try {
            const fetchPromises = context.basket
                .filter(item => !state.bookPrices[item.book_name])
                .map(async (item) => {
                    const book = await BookService.getBookByName(item.book_name);
                    prices[item.book_name] = book.price;
                    details[item.book_name] = book;
                });

            await Promise.all(fetchPromises);
            updateState({
                bookPrices: { ...state.bookPrices, ...prices },
                bookDetails: { ...state.bookDetails, ...details }
            });
        } catch (error) {
            console.error('Failed to load book details:', error);
            updateState({error: true});
        } finally {
            updateState({loading: false});
        }
    };

    useEffect(() => {
        loadBookDetails();
    }, [context.basket]);

    const calculateTotal = (): number => {
        return context.basket.reduce((total, item) => {
            const price = state.bookPrices[item.book_name] || 0;
            return total + (price * item.quantity);
        }, 0);
    };

    const getTotalItems = (): number => {
        return context.basket.reduce((total, item) => total + item.quantity, 0);
    };

    return <>
        ({state.loading && (<Icon topic='loading' size='big' />)}
        {state.error && (<Icon topic='error' size='big' />)}
        {!state.error && (
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
                        <MiniButton topic='cross' size='premedium' onClick={onClose}/>
                    </div>

                    <div className={styles.basketContent}>
                        {context.basket.length === 0 ? (
                            <div className={styles.emptyBasket}>
                                <h3>Your basket is empty!</h3>
                                <p>Add some books to get started!</p>
                            </div>
                        ) : (
                            <>
                                <div className={styles.basketItems}>
                                    {context.basket.map((item, index) => {
                                        const book = state.bookDetails[item.book_name];
                                        const price = state.bookPrices[item.book_name] || 0;
                                        return (
                                            <div key={`${item.book_name}-${index}`} className={styles.basketItem}>
                                                <div className={styles.itemInfo}>
                                                    <h4 className={styles.bookName}>{item.book_name}</h4>
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
                                                        <MiniButton topic='minus' size='mini' onClick={() => context.addToBasket(item.book_name, -1)} isDisabled={item.quantity <=1}/>
                                                        <span className={styles.quantity}>{item.quantity}</span>
                                                        <MiniButton topic='plus' size='mini' onClick={() => context.addToBasket(item.book_name)}/>
                                                    </div>
                                                    <MiniButton topic='bin' size='mini' onClick={() => context.removeFromBasket(item.book_name)}/>
                                                </div>
                                            </div>
                                        );
                                    })}
                                </div>

                                <div className={styles.basketFooter}>
                                    <div className={styles.totalSection}>
                                            <span className={styles.totalLabel}>Total:</span>
                                            <span className={styles.totalAmount}>${calculateTotal().toFixed(2)}</span>
                                    </div>

                                    <div className={styles.actionButtons}>
                                        <ActionButton type='cancel' onClick={context.clearBasket} disabled={state.loading || context.basket.length === 0}/>
                                        <ActionButton type='submit' onClick={handleOrder} disabled={state.loading || context.basket.length === 0}/>
                                    </div>
                                </div>
                            </>
                        )}
                    </div>
                </div>
            </div>
        )});
    </>
};