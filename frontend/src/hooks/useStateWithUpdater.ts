import { useState } from 'react';

export const useStateWithUpdater = <T>(initialState: T) => {
    const [state, setState] = useState<T>(initialState);

    const updateState = (updates: Partial<T>) => {
        setState(prevState => ({ ...prevState, ...updates }));
    };

    return [state, updateState] as const;
};