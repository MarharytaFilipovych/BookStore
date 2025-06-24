import React from 'react';
import styles from './style.module.css';
import {Icon} from '../Icon/Icon';
import {LinksSection} from '../LinkSection/LinkSection';
import {ContactProp, Links, User} from '../../types';

type FooterProps = {
    user: User,
    contacts: ContactProp[],
    links: Links
}

export const Footer: React.FC<FooterProps> = ({links, contacts, user}) => {
    const currentYear = new Date().getFullYear();
    return (
        <footer className={styles.footer}>
            <div className={styles.footerItems}>
                <div className={styles.aboutSection}>
                    <h4>Margosha book store</h4>
                    <p>Discover and explore the books which you may acquire in our store❤️.</p>
                </div>
                <div>
                    <h4>Links</h4>
                    <LinksSection links={links} style={'footer'} user={user}/>
                </div>
                <Contacts contacts={contacts}/>
            </div>
            <p className={styles.copyright}>&copy; {currentYear} TV Serieees. All rights reserved.</p>
        </footer>
    );
};

const Contacts: React.FC<{contacts: ContactProp[]}> = ({contacts}) => {
    return (
        <div className={styles.contactsSection}>
            <h4>Contacts</h4>
            <ul>
                {contacts.map((contact, index) => (
                    <Contact
                        key={index}
                        typeOfContact={contact.typeOfContact}
                        contact={contact.contact}
                    />
                ))}
            </ul>
        </div>
    );
};

const Contact: React.FC<ContactProp> = ({typeOfContact, contact}) => {
    return (
        <li className={styles.contact}>
            <Icon topic={typeOfContact === 'call' ? 'call' : 'envelope'} size='mini'/>
            <a href={typeOfContact === 'email' ? `mailto:${contact}` : `tel:${contact}`}>
                {contact}
            </a>
        </li>
    );
};