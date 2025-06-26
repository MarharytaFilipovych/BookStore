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

export const App: React.FC = () => {
    const context = React.useContext(AppContext);
    const [warning, setWarning] = useState<boolean>(false);
    const navigate = useNavigate();

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

            <div className={styles.wrapper}>
                {context.user && (
                    <>
                        <Header part='left'>
                            <MenuButton user={context.role!} links={links}/>
                        </Header>
                            <h1>Margosha book store</h1>
                        <Header part='right'>
                            <Link to={'/books'}><MiniButton topic='search' size='premedium'/></Link>

                            <h2>{context.user.name}</h2>
                            <AuthorizationButton type={'log-out'} onClick={()=> setWarning(true)}/>
                        </Header>
                    </>
                )}

                <Routes>
                    <Route path="/" element={<WelcomePage/>} />
                    <Route path="/login/:user" element={<LoginPage />} />
                    <Route path="/register" element={<RegistrationPage />} />
                    <Route path="/books" element={<div>Books Page</div>} />
                    <Route path="/basket" element={<div>Basket Page</div>} />
                    <Route path="/forgot" element={<ForgotPasswordPage/>}/>
                    <Route path="/sign" element={<RegistrationPage/>}/>
                    <Route path="/reset-password" element={<ResetPasswordPage/>}/>
                </Routes>

                {context.user && <Footer links={links} user={context.role!} contacts={myContacts}/>}
            </div>
        </>
    );
};