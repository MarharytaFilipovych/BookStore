import {AppContext} from "./context";
import React, {useEffect, useState} from "react";
import {Link, useLocation, useNavigate} from "react-router";
import {Warning} from "./components/Warning/Warning";
import {ActionButton} from "./components/AuthorizationButton/ActionButton";
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
import {BooksPage} from "./pages/DataPage/BooksPage";
import {PersonPage} from "./pages/DataPage/PersonPage";
import {OrdersPage} from "./pages/DataPage/OrderPage";
import {ProfilePage} from "./pages/AuthPage/ProfilePage";
import {Icon} from "./components/Icon/Icon";
import {ScrollToTop} from "./components/ScrollToTop/ScrollToTop";

export const App: React.FC = () => {
    const context = React.useContext(AppContext);
    const [warning, setWarning] = useState<boolean>(false);
    const navigate = useNavigate();
    const [isBasketOpen, setIsBasketOpen] = useState<boolean>(false);
    const location = useLocation();

    useEffect(() => {
        if (context.isLoading) return;
        const publicRoutes = ['/', '/login/client', '/login/employee', '/sign', '/forgot', '/reset-password'];
        const isPublicRoute = publicRoutes.includes(location.pathname);
        if (!context.user && !isPublicRoute) {
            console.log('🚪 User not authenticated, redirecting to welcome page...');
            navigate('/', { replace: true });
        }
    }, [context.user, context.isLoading, location.pathname, navigate]);

    return (
        <>

            <ScrollToTop />
            {warning && <Warning
                onClick={async ()=>{
                    await context.logout();
                    navigate('/');
                }}
                onCancel={()=>setWarning(false)}
                purpose='log-out'
                message={'Are you sure about logging out?'}
            />}
            {context.isLoading && <Icon topic='loading' size='big'/>}

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
                            {context.role === 'CLIENT' && (<MiniButton topic='basket' size='premedium' onClick={() => setIsBasketOpen(true)} />)}
                            <h2>{context.user.name}</h2>
                            <ActionButton type={'log-out'} onClick={()=> setWarning(true)}/>
                        </Header>
                    </Header>
                )}
                <div className={styles.contentArea}>
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
                        <Route path="/orders" element={<OrdersPage forWhom='all'/>}/>
                        <Route path="/my-orders" element={<OrdersPage forWhom={context.role === 'CLIENT'? 'client' : 'employee'}/>}/>
                    </Routes>
                </div>
                {context.user && <Footer links={links} user={context.role!} contacts={myContacts}/>}
            </div>
        </>
    );
};