import {AppContext} from "./context";
import React, {useState} from "react";
import {Link, useNavigate} from "react-router";
import {Warning} from "./components/Warning/Warning";
import {AuthorizationButton} from "./components/AuthorizationButton/AuthorizationButton";
import {WelcomePage} from "./pages/WelcomePage/WelcomePage";
import {Route, Routes} from "react-router-dom";
import {LoginPage} from "./pages/AuthPage/LoginPage";
import {RegistrationPage} from "./pages/AuthPage/RegistrationPage";
import {Footer} from "./components/Footer/Footer";
import {links, myContacts} from "./BusinessData";
import styles from './main.module.css';
import {ResetPasswordPage} from "./pages/AuthPage/ResetPasswordPage";
import {ForgotPasswordPage} from "./pages/AuthPage/ForgotPasswordPage";
import {Header} from "./components/Header/Header";
import {MenuButton} from "./components/MenuButton/MenuButton";
import {MiniButton} from "./components/MiniButton/MiniButton";
import {Basket} from "./components/Basket/Basket";
import {BasketButton} from "./components/Basket/BasketButton";
import {BooksPage} from "./pages/DataPage/BooksPage";
import {PersonPage} from "./pages/DataPage/PersonPage";
import {OrdersPage} from "./pages/DataPage/OrderPage";
import {ProfilePage} from "./pages/ProfilePage/ProfilePage";

export const App: React.FC = () => {
    const context = React.useContext(AppContext);
    const [warning, setWarning] = useState<boolean>(false);
    const navigate = useNavigate();
    const [isBasketOpen, setIsBasketOpen] = useState<boolean>(false);
    return (
        <>
            {warning && <Warning
                onClick={async ()=>{
                    await context.logout();
                    navigate('/');
                }}
                onCancel={()=>setWarning(false)}
                purpose='log-out'
                message={'Are you sure about logging out?'}
            />}

            {isBasketOpen && context.role === 'CLIENT' && (
                <Basket onClose={() => setIsBasketOpen(false)}/>
            )}

            <div className={styles.wrapper}>
                {context.user && (
                    <Header part='main'>
                        <Header part='left'>
                            <MenuButton user={context.role!} links={links}/>
                        </Header>
                            <h1>Margosha book store</h1>
                        <Header part='right'>
                            <Link to={'/books'}><MiniButton topic='search' size='premedium'/></Link>
                            {context.role === 'CLIENT' && (<BasketButton onClick={() => setIsBasketOpen(true)} /> )}
                            <h2>{context.user.name}</h2>
                            <AuthorizationButton type={'log-out'} onClick={()=> setWarning(true)}/>
                        </Header>
                    </Header>
                )}

                <Routes>
                    <Route path="/" element={<WelcomePage/>} />
                    <Route path="/login/:user" element={<LoginPage />} />
                    <Route path="/register" element={<RegistrationPage />} />
                    <Route path="/books" element={<BooksPage/>} />
                    <Route path="/basket" element={<div>Basket Page</div>} />
                    <Route path="/forgot" element={<ForgotPasswordPage/>}/>
                    <Route path="/sign" element={<RegistrationPage/>}/>
                    <Route path="/reset-password" element={<ResetPasswordPage/>}/>
                    <Route path="/people/:type" element={<PersonPage />} />
                    <Route path="/profile" element={<ProfilePage />} />
                    <Route path="/orders" element={<OrdersPage/>}/>
                </Routes>

                {context.user && <Footer links={links} user={context.role!} contacts={myContacts}/>}
            </div>
        </>
    );
};