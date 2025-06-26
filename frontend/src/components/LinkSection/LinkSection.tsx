import React from 'react';
import { Link } from 'react-router';
import styles from './style.module.css';
import classNames from 'classnames';
import {Links, Role, User} from '../../types';

export const LinksSection: React.FC<{ links: Links, style: 'menu' | 'footer', user: Role }> = ({ links, style, user }) => {
    return (
        <div>
            <ul className={classNames(styles.links, {
                [styles.footer]: style === 'footer',
                [styles.menu]: style === 'menu'
            })}>
                {user == 'EMPLOYEE' && links.employeeLinks.map((link, index) => (
                    <li key={`link-${index}`} className={styles.link}>
                        <Link to={`/${link.link}`}>{link.name}</Link>
                    </li>
                ))}
                {user == 'CLIENT' && links.clientLinks.map((link, index) => (
                    <li key={`userlink-${index}`} className={styles.link}>
                        <Link to={`/user/${link.link}`}>{link.name}</Link>
                    </li>
                ))}
            </ul>
        </div>
    );
};
