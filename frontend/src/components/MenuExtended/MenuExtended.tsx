import React, {useContext, useState} from 'react';
import styles from './style.module.css';
import {MiniButton} from '../MiniButton/MiniButton';
import {ActionButton} from '../AuthorizationButton/ActionButton';
import {useNavigate} from 'react-router';
import {LinksSection} from '../LinkSection/LinkSection';
import {Warning} from '../Warning/Warning';
import {Links, Role} from '../../types';
import {AppContext} from "../../context";

type MenuExtendedProps ={
    links: Links,
    onClose: ()=>void,
    user: Role
}

export const MenuExtended: React.FC<MenuExtendedProps>=({links,  onClose, user})=>{
    const [warning, setWarning] = useState(false);
    const context = useContext(AppContext);
    const navigate = useNavigate();
    return <>
        {warning && <Warning
             onClick={async ()=>{
                 setWarning(false);
                 await context.logout();
                 navigate('/');
             }}
             onCancel={()=>setWarning(false)}
             purpose={'log-out'}
             message={'Are you sure about logging out?'}/>}
        <div className={styles.menu}>
            <div className={styles.menuTop}>
                <h3>Menu</h3>
                <MiniButton topic='cross' size='medium' onClick={onClose}/>
            </div>

            <LinksSection links={links} style={'menu'} user={user}/>
            <div className={styles.auth}>
                <ActionButton type={'log-out'} onClick={()=>setWarning(true)}/>
            </div>
        </div>;
    </>;
};

