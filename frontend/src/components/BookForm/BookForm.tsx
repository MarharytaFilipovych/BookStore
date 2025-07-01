import React, { useState, useRef } from 'react';
import styles from './style.module.css';
import { BookType, Language, AgeGroup } from "../../types";
import { MiniButton } from "../MiniButton/MiniButton";
import { ActionButton } from "../AuthorizationButton/ActionButton";
import { ageGroups, genres, languages } from "../../BusinessData";
import {Icon} from "../Icon/Icon";

type BookFormProps = {
    onSubmit: (book: BookType) => void;
    onCancel: () => void;
    initialData?: Partial<BookType>;
    error: string,
    processing: boolean
};

export const BookForm: React.FC<BookFormProps> = ({ onSubmit, onCancel, initialData = {} , error, processing}) => {
    const isEditMode = Boolean(initialData.name);
    const formRef = useRef<HTMLFormElement>(null);
    const [errors, setErrors] = useState<Record<string, string>>({});

    const validateFormData = (formData: FormData): boolean => {
        const newErrors: Record<string, string> = {};

        const name = formData.get('name')?.toString()?.trim();
        const author = formData.get('author')?.toString()?.trim();
        const genre = formData.get('genre')?.toString()?.trim();
        const price = Number(formData.get('price')?.toString() || '0');
        const pages = Number(formData.get('pages')?.toString() || '0');
        const publicationDate = formData.get('publication_date')?.toString();
        const characteristics = formData.get('characteristics')?.toString()?.trim();
        const description = formData.get('description')?.toString()?.trim();

        if (!name) newErrors.name = 'Book name is required';
        if (!author) newErrors.author = 'Author is required';
        if (!genre) newErrors.genre = 'Genre is required';
        if (!price || price <= 0) newErrors.price = 'Price must be greater than 0';
        if (!pages || pages <= 0) newErrors.pages = 'Pages must be greater than 0';
        if (!publicationDate) newErrors.publication_date = 'Publication date is required';
        else {
            if (new Date(publicationDate) >= new Date()) {
                newErrors.publication_date = 'Publication date cannot be in the future';
            }
        }
        if (!characteristics) newErrors.characteristics = 'Characteristics are required';
        if (!description) newErrors.description = 'Description is required';

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        console.log('🔥 BookForm: handleSubmit called!');
        e.preventDefault();

        if (!formRef.current) {
            console.log('❌ Form ref is null');
            return;
        }

        const formData = new FormData(formRef.current);
        console.log('📝 Form data extracted:', {
            name: formData.get('name'),
            author: formData.get('author'),
            genre: formData.get('genre'),
            price: formData.get('price'),
            pages: formData.get('pages'),
            publication_date: formData.get('publication_date'),
            age_group: formData.get('age_group'),
            language: formData.get('language'),
            characteristics: formData.get('characteristics'),
            description: formData.get('description')
        });

        if (validateFormData(formData)) {
            console.log('✅ Validation passed, creating book object');

            const bookData: BookType = {
                name: formData.get('name')?.toString() || '',
                author: formData.get('author')?.toString() || '',
                genre: formData.get('genre')?.toString() || '',
                price: Number(formData.get('price')?.toString() || '0'),
                pages: Number(formData.get('pages')?.toString() || '0'),
                publication_date: formData.get('publication_date')?.toString() || '',
                age_group: formData.get('age_group')?.toString().toUpperCase() as AgeGroup || 'ADULT',
                language: formData.get('language')?.toString().toUpperCase() as Language || 'ENGLISH',
                characteristics: formData.get('characteristics')?.toString() || '',
                description: formData.get('description')?.toString() || ''
            };

            console.log('📤 Calling onSubmit with:', bookData);
            onSubmit(bookData);
        } else {
            console.log('❌ Validation failed, errors:', errors);
        }
    };

    return (
        <>
            {processing && (<Icon topic='loading' size='big' />)}
            <div className={styles.overlay}>
                <form className={styles.bookForm} onSubmit={handleSubmit} ref={formRef}>
                    <h2>{error}</h2>
                    <div className={styles.formHeader}>
                        <h2>{isEditMode ? 'Edit Book' : 'Create new book!'}</h2>
                        <MiniButton topic='cross' size='mini' onClick={onCancel}/>
                    </div>
                    <div className={styles.formContent}>
                        <div className={styles.formRow}>
                            <div className={styles.inputGroup}>
                                <label htmlFor="name">Book Name *</label>
                                <input
                                    id="name"
                                    name="name"
                                    type="text"
                                    defaultValue={initialData.name || ''}
                                    className={errors.name ? styles.errorInput : ''}
                                    placeholder="Enter book title"
                                    readOnly={isEditMode}
                                    required
                                />
                                {errors.name && <span className={styles.errorText}>{errors.name}</span>}
                                {isEditMode && <small className={styles.helpText}>Book name cannot be changed</small>}
                            </div>

                            <div className={styles.inputGroup}>
                                <label htmlFor="author">Author *</label>
                                <input
                                    id="author"
                                    name="author"
                                    type="text"
                                    defaultValue={initialData.author || ''}
                                    className={errors.author ? styles.errorInput : ''}
                                    placeholder="Enter author name"
                                    required
                                />
                                {errors.author && <span className={styles.errorText}>{errors.author}</span>}
                            </div>
                        </div>

                        <div className={styles.formRow}>
                            <div className={styles.inputGroup}>
                                <label htmlFor="genre">Genre *</label>
                                <select
                                    id="genre"
                                    name="genre"
                                    defaultValue={initialData.genre || ''}
                                    className={errors.genre ? styles.errorInput : ''}
                                    required
                                >
                                    <option value="">Select a genre</option>
                                    {genres.map(genre => (
                                        <option key={genre} value={genre}>{genre}</option>
                                    ))}
                                </select>
                                {errors.genre && <span className={styles.errorText}>{errors.genre}</span>}
                            </div>

                            <div className={styles.inputGroup}>
                                <label htmlFor="age_group">Age Group *</label>
                                <select
                                    id="age_group"
                                    name="age_group"
                                    defaultValue={initialData.age_group || 'ADULT'}
                                    required
                                >
                                    {ageGroups.map(ageGroup => (
                                        <option key={ageGroup} value={ageGroup}>{ageGroup}</option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        <div className={styles.formRow}>
                            <div className={styles.inputGroup}>
                                <label htmlFor="language">Language *</label>
                                <select
                                    id="language"
                                    name="language"
                                    defaultValue={initialData.language || 'ENGLISH'}
                                    required
                                >
                                    {languages.map(language => (
                                        <option key={language} value={language}>{language}</option>
                                    ))}
                                </select>
                            </div>

                            <div className={styles.inputGroup}>
                                <label htmlFor="publication_date">Publication Date *</label>
                                <input
                                    id="publication_date"
                                    name="publication_date"
                                    type="date"
                                    defaultValue={initialData.publication_date || ''}
                                    className={errors.publication_date ? styles.errorInput : ''}
                                    required
                                />
                                {errors.publication_date && <span className={styles.errorText}>{errors.publication_date}</span>}
                            </div>
                        </div>

                        <div className={styles.formRow}>
                            <div className={styles.inputGroup}>
                                <label htmlFor="price">Price ($) *</label>
                                <input
                                    id="price"
                                    name="price"
                                    type="number"
                                    min="0.01"
                                    step="0.01"
                                    defaultValue={initialData.price || ''}
                                    className={errors.price ? styles.errorInput : ''}
                                    placeholder="0.00"
                                    required
                                />
                                {errors.price && <span className={styles.errorText}>{errors.price}</span>}
                            </div>

                            <div className={styles.inputGroup}>
                                <label htmlFor="pages">Pages *</label>
                                <input
                                    id="pages"
                                    name="pages"
                                    type="number"
                                    min="1"
                                    defaultValue={initialData.pages || ''}
                                    className={errors.pages ? styles.errorInput : ''}
                                    placeholder="Enter number of pages"
                                    required
                                />
                                {errors.pages && <span className={styles.errorText}>{errors.pages}</span>}
                            </div>
                        </div>

                        <div className={styles.inputGroup}>
                            <label htmlFor="characteristics">Characteristics *</label>
                            <textarea
                                id="characteristics"
                                name="characteristics"
                                defaultValue={initialData.characteristics || ''}
                                className={errors.characteristics ? styles.errorInput : ''}
                                placeholder="Enter book characteristics (e.g., plot twists, colorful illustrations)"
                                rows={3}
                                required
                            />
                            {errors.characteristics && <span className={styles.errorText}>{errors.characteristics}</span>}
                        </div>

                        <div className={styles.inputGroup}>
                            <label htmlFor="description">Description *</label>
                            <textarea
                                id="description"
                                name="description"
                                defaultValue={initialData.description || ''}
                                className={errors.description ? styles.errorInput : ''}
                                placeholder="Enter detailed book description"
                                rows={4}
                                required
                            />
                            {errors.description && <span className={styles.errorText}>{errors.description}</span>}
                        </div>
                    </div>

                    <div className={styles.formActions}>
                        <ActionButton type='cancel' onClick={onCancel} />
                        <ActionButton type='submit' form={true} disabled={processing} />
                    </div>
                </form>
            </div>
        </>
    );
};