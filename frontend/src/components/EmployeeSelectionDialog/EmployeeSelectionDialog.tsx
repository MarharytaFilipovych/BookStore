import React from 'react';
import { EmployeeService } from '../../services/EmployeeService';
import { MiniButton } from '../MiniButton/MiniButton';
import styles from './style.module.css';
import {ActionButton} from "../AuthorizationButton/ActionButton";
import {useStateWithUpdater} from "../../hooks/useStateWithUpdater";

type EmployeeSelectionDialogProps = {
    isOpen: boolean;
    onClose: () => void;
    onSelectEmployee: (employeeEmail: string) => void;
}

type State = {
    email: string,
    isValidating: boolean,
    error: string
}

const initialState: State = {
    email: '',
    isValidating: false,
    error: ''
}

export const EmployeeSelectionDialog: React.FC<EmployeeSelectionDialogProps> = ({isOpen, onClose, onSelectEmployee}) => {
    const [state, setState] = useStateWithUpdater<State>(initialState);

    const handleConfirm = async () => {
        if (!state.email.trim()) return;
        setState({isValidating: true, error: ''})
        try {
            await EmployeeService.getEmployeeByEmail(state.email);
            onSelectEmployee(state.email);
            handleClose();
        } catch (error: any) {
            if (error.response?.status === 404) setState({error: 'Employee does not exist!'});
            else setState({error: 'Incorrect email!'});
        } finally {
            setState({isValidating: false})
        }
    };

    const handleClose = () => {
        setState({email: '', error: ''})
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className={styles.overlay} onClick={handleClose}>
            <div className={styles.dialog} onClick={(e) => e.stopPropagation()}>
                <div className={styles.dialogHeader}>
                    <h2>Type employee email...</h2>
                    <MiniButton topic='cross' size='premedium' onClick={handleClose} />
                </div>

                <div className={styles.dialogContent}>
                    <input
                        type="email"
                        className={`${styles.emailInput} ${state.error ? styles.emailInputError : ''}`}
                        placeholder="employee@gmail.com"
                        value={state.email}
                        onChange={(e) => {setState({email: e.target.value, error: ''});}}
                        onKeyDown={(e) => e.key === 'Enter' && handleConfirm()}
                        disabled={state.isValidating}
                        autoFocus
                    />
                    {state.error && <div className={styles.errorMessage}>{state.error}</div>}
                </div>

                <div className={styles.dialogActions}>
                    <ActionButton type='cancel' onClick={handleClose}/>
                    <ActionButton type='submit' onClick={handleConfirm} disabled={!state.email.trim() || state.isValidating}/>
                </div>
            </div>
        </div>
    );
};