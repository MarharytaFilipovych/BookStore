import {AppContext} from "./context";
import React, {useState} from "react";
import {useNavigate} from "react-router";
import {Warning} from "./components/Warning/Warning";
import {AuthorizationButton} from "./components/AuthorizationButton/AuthorizationButton";
import {WelcomePage} from "./pages/WelcomePage/WelcomePage";
import {Route, Routes} from "react-router-dom";
import {LoginPage} from "./pages/LoginPage/LoginPage";
import {RegistrationPage} from "./pages/LoginPage/RegistrationPage";
import {Footer} from "./components/Footer/Footer";
import {links, myContacts} from "./BusinessData";

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

            <div className="app-wrapper">
                {/* Header component would go here */}
                <header className="app-header">
                    <div className="header-left">
                        {/* Menu button */}
                    </div>
                    <h1>BookStore</h1>
                    <div className="header-right">
                        {context.user  ? (
                            <>
                                <span>Welcome, {context.user?.name}!</span>
                                <AuthorizationButton type={'log-out'} onClick={()=> setWarning(true)}/>

                            </>
                        ) : <WelcomePage/>}
                    </div>
                </header>

                {/* Main content area */}
                <main className="content-area">
                    <Routes>
                        <Route path="/" element={<WelcomePage/>} />
                        <Route path="/login/:userType" element={<LoginPage />} />
                        <Route path="/register" element={<RegistrationPage />} />
                        <Route path="/books" element={<div>Books Page</div>} />
                        <Route path="/basket" element={<div>Basket Page</div>} />
                        {/* Add more routes as needed */}
                    </Routes>
                </main>

                {context.user && <Footer links={links} user={context.role!} contacts={myContacts}/>}
            </div>
        </>
    );
};
