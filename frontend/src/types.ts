
export type IconTopic =  'search' | 'tick' | 'star' | 'vote'
    | 'cross' | 'envelope' | 'call' | 'empty-star'
    | 'black-cross' | 'caret' | 'heart' | 'empty-heart'
    | 'empty-circle' | 'circle' | 'loading' | 'error'
    | 'direction' | 'hidden' | 'plus';

export type ContactProp = {
    typeOfContact: 'email' | 'call',
    contact: string
}

export type LinkItem = {
    name: string,
    request_type: requestType
}

export type Links = {
    employeeLinks: LinkItem[],
    clientLinks: LinkItem[]
}


export type FilterState = {
    genre: string,
    language: string,
    country: string,
    sortOption: string,
    year: string,
    name: string
}

export type ConfigurationData = {
    countries: Map<string, string>,
    languages: Map<string, string>,
    genres: Map<string, string>,
    code_languages: Map<string, string>
}

export type requestType = 'clients' | 'orders' | 'books' | 'colleagues' | 'profile';


export type UserCollections = {
    favorites: Map<number, string>,
    future: Map<number, string>,
    watched: Map<number, string>
}

export type State = {
    loading: boolean,
    error: boolean
}

export type StateWithPagination = State & {
    pageToFetch: number,
    currentPage: number
}


export type User = 'CLIENT' | 'EMPLOYEE';

export type Language = 'ENGLISH'| 'SPANISH'| 'FRENCH'| 'GERMAN'| 'UKRAINIAN'| 'JAPANESE'| 'OTHER';

export type AgeGroup = 'CHILD'| 'TEEN'| 'ADULT'| 'OTHER';

export type Book = {
    name: string,
    genre: string,
    ageGroup: AgeGroup,
    price: number,
    publicationDate: Date,
    author: string,
    pages: number,
    characteristics: string,
    description: string,
    language: Language
}