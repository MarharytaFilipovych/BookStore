import React, {useContext, useState} from 'react';
import styles from './style.module.css';
import {OrderType} from "../../types";
import {AppContext} from "../../context";
import {MiniButton} from "../MiniButton/MiniButton";

type OrderComponentProps = OrderType & {
    onAssignEmployee?: (orderId: string) => void;
};

export const OrderComponent: React.FC<OrderComponentProps> = (order) => {
    const context = useContext(AppContext);
    const [showDetails, setShowDetails] = useState(false);

    const toggleDetails = () => {
        setShowDetails(!showDetails);
    };

    const handleAssignEmployee = () => {
        if (order.onAssignEmployee) {
            // You can pass order date or another unique identifier
            order.onAssignEmployee(order.order_date);
        }
    };

    // Format date for display
    const formatDate = (dateString: string): string => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    // Get order status based on employee assignment
    const getOrderStatus = () => {
        return order.employee_email ? 'Assigned' : 'Pending';
    };

    // Calculate total items in order
    const getTotalItems = () => {
        return order.book_items.reduce((total, item) => total + item.quantity, 0);
    };

    return (
        <>
            <div className={styles.orderContainer}>
                <div className={styles.orderInfo}>
                    <h3
                        className={styles.orderDate}
                        onClick={toggleDetails}
                    >
                        Order #{formatDate(order.order_date)}
                    </h3>
                    <p className={styles.clientEmail}>Client: {order.client_email}</p>
                </div>

                <div className={styles.orderMeta}>
                    <div className={styles.employeeInfo}>
                        <span className={styles.employeeLabel}>Employee:</span>
                        <span className={styles.employeeEmail}>
                            {order.employee_email || 'Not assigned'}
                        </span>
                    </div>
                    <div className={styles.orderStats}>
                        <span className={styles.itemCount}>{getTotalItems()} items</span>
                        <span className={styles.orderPrice}>${order.price}</span>
                    </div>
                </div>

                <div className={styles.orderActions}>
                    <div className={`${styles.statusBadge} ${styles[getOrderStatus().toLowerCase()]}`}>
                        {getOrderStatus()}
                    </div>
                    {!order.employee_email && (
                        <MiniButton
                            topic='plus'
                            size='medium'
                            onClick={handleAssignEmployee}
                        />
                    )}
                </div>
            </div>

            {showDetails && (
                <div className={styles.overlay} onClick={toggleDetails}>
                    <article className={styles.orderDescription} onClick={(e) => e.stopPropagation()}>
                        <div className={styles.orderDetailsHeader}>
                            <h2>Order Details</h2>
                            <MiniButton
                                topic='cross'
                                size='mini'
                                onClick={toggleDetails}
                            />
                        </div>
                        <div className={styles.orderDetailsContent}>
                            <div className={styles.orderSummary}>
                                <div className={styles.detailRow}>
                                    <strong>Order Date:</strong>
                                    <span>{formatDate(order.order_date)}</span>
                                </div>
                                <div className={styles.detailRow}>
                                    <strong>Client Email:</strong>
                                    <span>{order.client_email}</span>
                                </div>
                                <div className={styles.detailRow}>
                                    <strong>Assigned Employee:</strong>
                                    <span className={order.employee_email ? styles.assigned : styles.unassigned}>
                                        {order.employee_email || 'Not assigned'}
                                    </span>
                                </div>
                                <div className={styles.detailRow}>
                                    <strong>Total Price:</strong>
                                    <span className={styles.totalPrice}>${order.price}</span>
                                </div>
                                <div className={styles.detailRow}>
                                    <strong>Status:</strong>
                                    <span className={`${styles.statusIndicator} ${styles[getOrderStatus().toLowerCase()]}`}>
                                        {getOrderStatus()}
                                    </span>
                                </div>
                            </div>

                            <div className={styles.bookItemsSection}>
                                <h3>Ordered Books</h3>
                                {order.book_items.length > 0 ? (
                                    <div className={styles.bookItemsList}>
                                        {order.book_items.map((item, index) => (
                                            <div key={index} className={styles.bookItem}>
                                                <div className={styles.bookItemInfo}>
                                                    <strong>{item.book_name}</strong>
                                                    <span className={styles.quantity}>Qty: {item.quantity}</span>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                ) : (
                                    <p className={styles.emptyItems}>No books in this order</p>
                                )}
                            </div>
                        </div>
                    </article>
                </div>
            )}
        </>
    );
};