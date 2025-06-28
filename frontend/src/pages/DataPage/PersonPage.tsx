import React, {useCallback, useEffect, useState} from "react";
import { useParams} from "react-router-dom";
import { PersonFilterState, SortOrder, ClientType, EmployeeType, ClientSortField, EmployeeSortField, StateWithPagination } from "../../types";
import { PersonSearchField } from "../../components/Search/PersonSearchField";
import { clientSortOptionsWithMappings, employeeSortOptionsWithMappings } from "../../BusinessData";
import { GenericSearchablePage } from "./GenereicSearchablePage";
import { ClientComponent } from "../../components/Person/Client";
import { EmployeeComponent } from "../../components/Person/Employee";
import { ClientService } from "../../services/ClientService";
import { EmployeeService } from "../../services/EmployeeService";
import { useStateWithUpdater } from "../../hooks/useStateWithUpdater";
import {Icon} from "../../components/Icon/Icon";

type PersonType = ClientType | EmployeeType;
type PersonSortField = ClientSortField | EmployeeSortField;

type ExtendedPageState = {
    blockedClients: Set<string>;
    isLoadingBlockedList: boolean;
    refreshTrigger: number;
};

export const PersonPage: React.FC = () => {
    const { type } = useParams<{ type: string }>();
    const isClientsPage = type === 'clients' || type === 'blocked';

    const [extendedState, setExtendedState] = useStateWithUpdater<ExtendedPageState>({
        blockedClients: new Set(),
        isLoadingBlockedList: false,
        refreshTrigger: 0
    });

    const getFilterState = (searchParams: URLSearchParams): PersonFilterState => ({
        email: searchParams.get('email') ?? '',
        sort: searchParams.get('sort') ?? ''
    });

    const fetchBlockedClients = useCallback(async () => {
        if (!isClientsPage) return;
        setExtendedState(({isLoadingBlockedList: true }));
        try {
            const blockedClientsResponse = await ClientService.getBlockedClientsList();
            const blockedEmails = new Set(blockedClientsResponse.map(client => client.email));
            setExtendedState(({blockedClients: blockedEmails, isLoadingBlockedList: false}));
            console.log(`Loaded ${blockedEmails.size} blocked clients`);
        } catch (error) {
            console.error('Failed to fetch blocked clients:', error);
            setExtendedState(({blockedClients: new Set(), isLoadingBlockedList: false}));
        }
    }, [isClientsPage, setExtendedState]);

    const fetchPersons = async (
        page: number,
        pageSize: number,
        filter: PersonFilterState,
        sorting?: { sortBy: PersonSortField; sortOrder: SortOrder }
    ): Promise<{ meta: { totalPages: number; total_count: number }; items: PersonType[] }> => {
        if (isClientsPage) {
            let response;
            if(type === 'blocked'){
                response = await ClientService.getBlockedClients(
                    page,
                    pageSize,
                    sorting?.sortBy as ClientSortField,
                    sorting?.sortOrder
                );
            }else{
                response = await ClientService.getClients(
                    page,
                    pageSize,
                    sorting?.sortBy as ClientSortField,
                    sorting?.sortOrder
                );
            }
            return {
                meta: {
                    totalPages: response.meta.totalPages,
                    total_count: response.meta.total_count
                },
                items: (response.clients || []) as PersonType[]
            };
        } else {
            const response = await EmployeeService.getEmployees(
                page,
                pageSize,
                sorting?.sortBy as EmployeeSortField,
                sorting?.sortOrder
            );
            return {
                meta: {
                    totalPages: response.meta.totalPages,
                    total_count: response.meta.total_count
                },
                items: (response.employees || []) as PersonType[]
            };
        }
    };

    const handleBlockClient = useCallback(async (email: string): Promise<void> => {
        setExtendedState({isLoadingBlockedList: true})
        try {
            await ClientService.blockClient(email);
            await fetchBlockedClients();
            console.log(`Client ${email} blocked successfully`);
        } catch (error) {
            console.error('Failed to block client:', error);
            alert(`Could not block client with email ${email}`);
        }finally {
            setExtendedState({isLoadingBlockedList: false})
        }
    }, [fetchBlockedClients]);

    const handleUnblockClient = useCallback(async (email: string): Promise<void> => {
        setExtendedState({isLoadingBlockedList: true})
        try {
            await ClientService.unblockClient(email);
            await fetchBlockedClients();
            setExtendedState({refreshTrigger: extendedState.refreshTrigger + 1})
            console.log(`Client ${email} unblocked successfully`);
        } catch (error) {
            console.error('Failed to unblock client:', error);
            alert(`Could not unblock client with email ${email}`);
        }finally {
            setExtendedState({isLoadingBlockedList: false})
        }
    }, [fetchBlockedClients]);

    const renderSearchComponent = ({ filter, onFilterChange }: {
        filter: PersonFilterState;
        onFilterChange: (key: keyof PersonFilterState, value: string) => void;
    }) => (
        <PersonSearchField
            sortOptions={Array.from(
                isClientsPage
                    ? clientSortOptionsWithMappings.keys()
                    : employeeSortOptionsWithMappings.keys()
            )}
            filter={filter}
            onFilterChange={onFilterChange}
        />
    );

    const renderPerson = (person: PersonType, index: number) => {
        if (isClientsPage) {
            const client = person as ClientType;
            return (
                <ClientComponent
                    key={`${client.email}-${index}`}
                    name={client.name}
                    email={client.email}
                    balance={client.balance}
                    isBlocked={extendedState.blockedClients.has(client.email)}
                    onBlock={handleBlockClient}
                    onUnblock={handleUnblockClient}
                />
            );
        } else {
            const employee = person as EmployeeType;
            return (
                <EmployeeComponent
                    key={`${employee.email}-${index}`}
                    name={employee.name}
                    email={employee.email}
                    phone={employee.phone}
                    birthdate={employee.birthdate}
                />
            );
        }
    };

    return (
        <>
            {extendedState.isLoadingBlockedList && <Icon topic='loading' size='big'/>}
            <GenericSearchablePage<PersonType, PersonFilterState, PersonSortField>
                fetchData={fetchPersons}
                getFilterFromParams={getFilterState}
                sortOptions={isClientsPage ? clientSortOptionsWithMappings : employeeSortOptionsWithMappings}
                searchComponent={renderSearchComponent}
                renderItem={renderPerson}
                resultsCountText={(count) => {
                    if(isClientsPage) return `Found ${count} ${count === 1 ? 'client' : 'clients'}!`
                    return `Found ${count} ${count === 1 ? 'colleague' : 'colleagues'}!`
                }}
                noResultsMessage={
                    isClientsPage
                        ? "No clients found! Try adjusting your search criteria!"
                        : "No colleagues found! Try adjusting your search criteria!"
                }
                refreshTrigger={extendedState.refreshTrigger}
            />
        </>
    );
};