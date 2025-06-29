import React from 'react';
import styles from './style.module.css';
import classNames from 'classnames';
import search from './icons/search.png';
import star from './icons/star.png';
import call from './icons/call.png';
import envelope from './icons/envelope.png';
import cross from './icons/cross.png';
import blackCross from './icons/black-cross.png';
import caret from './icons/caret.png';
import loading from './icons/loading.png';
import error from './icons/error.png';
import plus from './icons/plus.png';
import basket from './icons/shopping-basket.png';
import bin from './icons/bin.png';
import ban from './icons/ban.png';
import update from './icons/update.png';
import unban from './icons/unban.png';
import minus from  './icons/minus.png';

export type IconTopic = 'search'| 'star' | 'cross' | 'envelope' | 'call' | 'black-cross' | 'caret' | 'loading' | 'error'|
    'plus' | 'basket' | 'bin' | 'ban' | 'update' | 'unban' | 'minus';

type IconProps ={
    topic: IconTopic;
    size: 'mini' | 'medium' | 'big' | 'premedium'
    mirror?: boolean;
}

export const Icon: React.FC<IconProps> = ({topic, size, mirror})=>{
    const getSource = () => {
        switch(topic) {
            case 'search': return search;
            case 'star': return star;
            case 'envelope': return envelope;
            case 'call': return call;
            case 'cross': return cross;
            case 'black-cross' : return blackCross;
            case 'caret': return caret;
            case 'loading': return loading;
            case 'error': return error;
            case 'plus': return plus;
            case 'basket': return basket;
            case 'bin' : return bin;
            case 'ban': return ban;
            case 'update': return update;
            case 'unban': return unban;
            case 'minus': return minus;
        }
    };
    return (
        <img
            className={classNames({
                [styles.mini]: size === 'mini',
                [styles.medium]: size === 'medium',
                [styles.mirror]: mirror,
                [styles.loading]: topic === 'loading',
                [styles.big]: size === 'big',
                [styles.premedium]: size === 'premedium'
            })}
            src={getSource()}
            alt={`${topic} icon`}
        />
    );
};