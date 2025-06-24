import React, {useContext, useState} from 'react';
import styles from './style.module.css';
import {MiniButton} from '../MiniButton/MiniButton';
import {AuthorizationButton} from '../AuthorizationButton/AuthorizationButton';
import {Link} from 'react-router';
import {LinksSection} from '../LinkSection/LinkSection';
import {Warning} from '../Warning/Warning';
//import {AppContext} from '../../context';
import {Links, Role} from '../../types';

type MenuExtendedProps ={
    links: Links,
    onClose: ()=>void,
    user: Role
}


export const MenuExtended: React.FC<MenuExtendedProps>=({links,  onClose, user})=>{
    const [warning, setWarning] = useState(false);
    //const context = useContext(AppContext);

    return <>
        {warning && <Warning
             onClick={()=>{
                 setWarning(false);
              //   context.cleanUser();
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

            <AuthorizationButton type={'log-out'} onClick={()=>setWarning(true)}/>

        </div>;
    </>;
};

