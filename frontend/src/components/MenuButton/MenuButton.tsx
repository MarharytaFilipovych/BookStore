import React, {useState} from 'react';
import styles from './style.module.css';
import {MenuExtended} from '../MenuExtended/MenuExtended';
import {Links, Role} from '../../types';


export const MenuButton: React.FC<{user: Role, links: Links}> = ({user, links})=>{
    const [isMenuOpen, setIsMenuOpen] = useState(false);

    return <><button className={styles.menuComponent} onClick={()=>setIsMenuOpen(true)}>
        <div className={styles.menu}>
            <Line/>
            <Line/>
            <Line/>
        </div>
        <p className={styles.menuText}>Menu</p>
    </button>
        {isMenuOpen && <MenuExtended user={user} links={links} onClose={()=>setIsMenuOpen(false)}/>}
    </>;
};

const Line: React.FC = ()=>{
    return <div className={styles.line}></div>;
};