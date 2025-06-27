import React from "react";
import { useParams} from "react-router-dom";
import { PersonFilterState, SortOrder, ClientType, EmployeeType, ClientSortField, EmployeeSortField } from "../../types";
import { PersonSearchField } from "../../components/Search/PersonSearchField";
import { clientSortOptionsWithMappings, employeeSortOptionsWithMappings } from "../../BusinessData";
import styles from './style.module.css';
import { GenericSearchablePage } from "./GenereicSearchablePage";
import { ClientComponent } from "../../components/Client/Client";
import { EmployeeComponent } from "../../components/Employee/Employee";
import { ClientService } from "../../services/ClientService";
import { EmployeeService } from "../../services/EmployeeService";

type PersonType = ClientType | EmployeeType;
type PersonSortField = ClientSortField | EmployeeSortField;

export const PersonPage: React.FC = () => {
    const { type } = useParams<{ type: string }>();
    const isClientsPage = type === 'clients';

    const getFilterState = (searchParams: URLSearchParams): PersonFilterState => ({
        email: searchParams.get('email') ?? '',
        sort: searchParams.get('sort') ?? ''
    });

    const fetchPersons = async (
        page: number,
        pageSize: number,
        filter: PersonFilterState,
        sorting?: { sortBy: PersonSortField; sortOrder: SortOrder }
    ): Promise<{ meta: { totalPages: number; total_count: number }; items: PersonType[] }> => {
        if (isClientsPage) {
            const response = await ClientService.getClients(
                page,
                pageSize,
                sorting?.sortBy as ClientSortField,
                sorting?.sortOrder
            );
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
        <GenericSearchablePage<PersonType, PersonFilterState, PersonSortField>
            fetchData={fetchPersons}
            getFilterFromParams={getFilterState}
            sortOptions={isClientsPage ? clientSortOptionsWithMappings : employeeSortOptionsWithMappings}
            searchComponent={renderSearchComponent}
            renderItem={renderPerson}
            itemsContainerClassName={styles.clientsContainer}
            noResultsMessage={
                isClientsPage
                    ? "No clients found! Try adjusting your search criteria!"
                    : "No colleagues found! Try adjusting your search criteria!"
            }
        />
    );
};