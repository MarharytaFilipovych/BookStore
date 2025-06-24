import React from 'react';
import styles from './style.module.css';
import {PageNavigator} from '../PageNavigator/PageNavigator';
import {Button} from '../Button/Button';

type PaginationProps = {
    pageCount: number,
    onPageSelect: (page: number)=>void,
    onClick: ()=>void,
    page: number
    disabledShowMoreButton: boolean
}
export const Pagination: React.FC<PaginationProps> = ({pageCount, onPageSelect, onClick, page, disabledShowMoreButton})=>{
    return <div className={styles.pagination}>
        <PageNavigator page={page} pageCount={pageCount} onPageSelect={onPageSelect}/>
        <Button purpose='Show more' onClick={onClick} disabled={disabledShowMoreButton}/>
    </div>;
};