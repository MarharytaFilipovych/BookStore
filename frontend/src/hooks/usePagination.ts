import {useEffect, useState} from 'react';

const determinePageToShow = (activePage: number, pageCount: number): number[] =>{
    if(pageCount <= 5 || activePage <= 3) return Array.from({length: Math.min(5, pageCount)}, (_, i)=>i+1);
    if(activePage > pageCount - 3) return Array.from({length: 5}, (_, i)=> pageCount - 4 + i);
    return [activePage-2, activePage-1, activePage, activePage+1, activePage+2];
};

type PaginationState = {
    currentPage: number;
    visiblePages: number[];
    buttonStates: {
        leftArrow: boolean;
        rightArrow: boolean;
    };
    onPageChange: (page: number) => void;
    onLeftArrowClick: () => void;
    onRightArrowClick: () => void;
}

export const usePagination = (initialPage: number,pageCount: number, onPageSelect: (page: number) => void): PaginationState =>{
    const [currentPage, setCurrentPage] = useState(initialPage);
    const [visiblePages, setVisiblePages] = useState(determinePageToShow(initialPage, pageCount));
    const [buttonStates, setButtonStates] = useState({
        leftArrow: visiblePages[0] > 1,
        rightArrow: visiblePages[visiblePages.length - 1] < pageCount
    });
    const onPageChange = (newPage: number) => {
        onPageSelect(newPage);
        setCurrentPage(newPage);
        setVisiblePages(determinePageToShow(newPage, pageCount));
    };

    const onLeftArrowClick = () => {
        if (visiblePages[0] <= 1) return;
        setVisiblePages(visiblePages.map(p => p - 1));
    };

    const onRightArrowClick = () => {
        if (visiblePages[visiblePages.length - 1] >= pageCount) return;
        setVisiblePages(visiblePages.map(p => p + 1));
    };

    useEffect(() => {
        setButtonStates({
            leftArrow: visiblePages[0] === 1,
            rightArrow: visiblePages[visiblePages.length - 1] === pageCount
        });
    }, [visiblePages, pageCount]);

    useEffect(() => {
        setVisiblePages(determinePageToShow(currentPage, pageCount));
    }, [pageCount, currentPage]);

    return {
        currentPage,
        visiblePages,
        buttonStates,
        onPageChange,
        onLeftArrowClick,
        onRightArrowClick
    };
};