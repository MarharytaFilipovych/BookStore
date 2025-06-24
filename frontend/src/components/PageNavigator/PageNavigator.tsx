import React from 'react';
import styles from './style.module.css';
import classNames from 'classnames';
import { MiniButton } from '../MiniButton/MiniButton';
import { usePagination } from '../../hooks/usePagination';

type PageItemProps = {
    type: 'active' | 'normal',
    index: number,
    onClick: () => void,
}

export const PageNavigator: React.FC<{ page: number, pageCount: number, onPageSelect: (page: number) => void }> = ({ page, pageCount, onPageSelect }) => {
    const {currentPage, visiblePages, buttonStates, onPageChange, onLeftArrowClick, onRightArrowClick} = usePagination(page, pageCount, onPageSelect);
    return (
        <div className={styles.pages}>
            <MiniButton
                topic='caret'
                isDisabled={buttonStates.leftArrow}
                size='medium'
                onClick={onLeftArrowClick}
            />
            {visiblePages.map(page => (
                <PageItem
                    key={page}
                    onClick={() =>onPageChange(page)}
                    type={page === currentPage ? 'active' : 'normal'}
                    index={page}
                />
            ))}
            <MiniButton
                topic='caret'
                mirror={true}
                isDisabled={buttonStates.rightArrow}
                size='medium'
                onClick={onRightArrowClick}
            />
        </div>
    );
};

const PageItem: React.FC<PageItemProps> = ({ type, index, onClick }) => {
    return (
        <a
            className={classNames(styles.page, {
                [styles.active]: type === 'active'
            })}
            href='#'
            onClick={e => {
                e.preventDefault();
                onClick();
            }}
        >
            {index}
        </a>
    );
};