import React, {useContext, useState} from 'react';
import styles from './style.module.css';
import {OrderType} from "../../types";
import {MiniButton} from "../MiniButton/MiniButton";
import {AppContext} from "../../context";

type OrderComponentProps = OrderType & {
    onAssignEmployee?: (orderId: string) => void;
};

export const OrderComponent: React.FC<OrderComponentProps> = (order) => {
    const [showDetails, setShowDetails] = useState(false);
    const context = useContext(AppContext);

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

    const getOrderStatus = () => {
        return order.employee_email ? 'assigned' : 'pending';
    };

    const getTotalItems = () => {
        return order.book_items.reduce((total, item) => total + item.quantity, 0);
    };

    return (
        <>
            <div className={styles.orderContainer}>
                <div className={styles.orderInfo}>
                    <h3 className={styles.orderDate} onClick={()=>setShowDetails(!showDetails)}>
                        {formatDate(order.order_date)}</h3>
                    <a href={`mailto:${order.client_email}`} className={styles.clientEmail}>Client: {order.client_email || 'lost client:('}</a>
                </div>

                <div className={styles.orderMeta}>
                    <div className={styles.employeeInfo}>
                        <p className={styles.employeeLabel}>Employee:
                            {order.employee_email ? (
                                <a href={`mailto:${order.employee_email}`} className={styles.employeeEmail}>
                                    {order.employee_email}
                                </a>
                            ) : (
                                <span className={styles.employeeEmail}>not confirmed</span>
                            )}
                        </p>
                    </div>
                    <div className={styles.orderStats}>
                        <span className={styles.itemCount}>{getTotalItems()} items</span>
                        <span className={styles.orderPrice}>${order.price}</span>
                    </div>
                </div>

                {context.role === 'EMPLOYEE' && (
                    <div className={styles.orderActions}>
                    <div className={`${styles.statusBadge} ${styles[getOrderStatus()]}`}>{getOrderStatus()}</div>
                    {!order.employee_email && (<MiniButton topic='plus' size='medium' onClick={()=>{
                        if (order.onAssignEmployee) order.onAssignEmployee(order.order_date);
                    }}/>)}
                </div>)}
            </div>

            {showDetails && (
                <div className={styles.overlay} onClick={()=>setShowDetails(!showDetails)}>
                    <article className={styles.orderDescription} onClick={(e) => e.stopPropagation()}>
                        <div className={styles.orderDetailsHeader}>
                            <h2>Order details</h2>
                            <MiniButton topic='cross' size='mini' onClick={()=>setShowDetails(!showDetails)}/>
                        </div>
                        <div className={styles.orderDetailsContent}>
                            <div className={styles.orderSummary}>
                                <div className={styles.detailRow}>
                                    <strong>Order Date:</strong>
                                    <span>{formatDate(order.order_date)}</span>
                                </div>
                                <div className={styles.detailRow}>
                                    <strong>Client email:</strong>
                                    <span>
                                        {order.client_email ? (
                                            <a href={`mailto:${order.client_email}`}>
                                                {order.client_email}
                                            </a>
                                        ) : (
                                            "lost client :(("
                                        )}
                                    </span>
                                </div>
                                <div className={styles.detailRow}>
                                    <strong>Assigned employee:</strong>
                                    <span className={order.employee_email ? styles.assigned : styles.unassigned}>
                                        {order.employee_email ? (
                                            <a href={`mailto:${order.employee_email}`}>
                                                {order.employee_email}
                                            </a>
                                        ) : (
                                            'not confirmed'
                                        )}
                                    </span>
                                </div>
                                <div className={styles.detailRow}>
                                    <strong>Total price:</strong>
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
                                <h3>Ordered books</h3>
                                {order.book_items.length > 0 ? (
                                    <div className={styles.bookItemsList}>
                                        {order.book_items.map((item, index) => (
                                            <div key={index} className={styles.bookItem}>
                                                <div className={styles.bookItemInfo}>
                                                    <strong>{item.book_name}</strong>
                                                    <span className={styles.quantity}>Quantity: {item.quantity}</span>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                ) : (
                                    <p className={styles.emptyItems}>No books in this order!</p>
                                )}
                            </div>
                        </div>
                    </article>
                </div>
            )}
        </>
    );
};