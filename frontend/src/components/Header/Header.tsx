import React, {ReactNode} from 'react';
import styles from './style.module.css';
import classNames from 'classnames';

export const Header: React.FC<{ children: ReactNode, part: 'left' | 'right' | 'main' }> = ({children, part}) => {
    return <header className={classNames({
        [styles.header]: part === 'main',
        [styles.headerRightPart]: part === 'right',
        [styles.headerLeftPart]: part === 'left',
    })}>{children}</header>;
};