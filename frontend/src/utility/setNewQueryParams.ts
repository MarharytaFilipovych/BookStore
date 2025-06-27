export const setNewQueryParams = <T extends Record<string, any>>(
    key: keyof T,
    value: string,
    searchParams: URLSearchParams
): URLSearchParams => {
    const newParams = new URLSearchParams(searchParams);
    if (value) newParams.set(key as string, value);
    else newParams.delete(key as string);
    newParams.set('page', '1');
    return newParams;
};
