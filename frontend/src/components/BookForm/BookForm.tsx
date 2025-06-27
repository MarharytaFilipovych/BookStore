import React, { useState } from 'react';
import styles from './style.module.css';
import { BookType, Language, AgeGroup } from "../../types";
import { MiniButton } from "../MiniButton/MiniButton";
import { AuthorizationButton } from "../AuthorizationButton/AuthorizationButton";
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

    const [formData, setFormData] = useState<Partial<BookType>>({
        ...initialData
    });

    const [errors, setErrors] = useState<Record<string, string>>({});

    const handleInputChange = (field: keyof BookType, value: string | number) => {
        setFormData(prev => ({
            ...prev,
            [field]: value
        }));

        if (errors[field]) {
            setErrors(prev => ({
                ...prev,
                [field]: ''
            }));
        }
    };

    const validateForm = (): boolean => {
        const newErrors: Record<string, string> = {};

        if (!formData.name?.trim()) newErrors.name = 'Book name is required';
        if (!formData.author?.trim()) newErrors.author = 'Author is required';
        if (!formData.genre?.trim()) newErrors.genre = 'Genre is required';
        if (!formData.price || formData.price <= 0) newErrors.price = 'Price must be greater than 0';
        if (!formData.pages || formData.pages <= 0) newErrors.pages = 'Pages must be greater than 0';
        if (!formData.publication_date) newErrors.publication_date = 'Publication date is required';
        else {
            if (new Date(formData.publication_date) > new Date()) {
                newErrors.publication_date = 'Publication date cannot be in the future';
            }
        }
        if (!formData.characteristics?.trim()) newErrors.characteristics = 'Characteristics are required';
        if (!formData.description?.trim()) newErrors.description = 'Description is required';

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (validateForm()) onSubmit(formData as BookType);
    };

    return (
        <>
            {processing && (<Icon topic='loading' size='big' />)}
            <div className={styles.overlay}>
                <form className={styles.bookForm} onSubmit={handleSubmit}>
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
                                    type="text"
                                    value={formData.name || ''}
                                    onChange={(e) => handleInputChange('name', e.target.value)}
                                    className={errors.name ? styles.errorInput : ''}
                                    placeholder="Enter book title"
                                    disabled={isEditMode}
                                />
                                {errors.name && <span className={styles.errorText}>{errors.name}</span>}
                                {isEditMode && <small className={styles.helpText}>Book name cannot be changed</small>}
                            </div>

                            <div className={styles.inputGroup}>
                                <label htmlFor="author">Author *</label>
                                <input
                                    id="author"
                                    type="text"
                                    value={formData.author || ''}
                                    onChange={(e) => handleInputChange('author', e.target.value)}
                                    className={errors.author ? styles.errorInput : ''}
                                    placeholder="Enter author name"
                                />
                                {errors.author && <span className={styles.errorText}>{errors.author}</span>}
                            </div>
                        </div>

                        <div className={styles.formRow}>
                            <div className={styles.inputGroup}>
                                <label htmlFor="genre">Genre *</label>
                                <select
                                    id="genre"
                                    value={formData.genre || ''}
                                    onChange={(e) => handleInputChange('genre', e.target.value)}
                                    className={errors.genre ? styles.errorInput : ''}
                                >
                                    <option value="">Select genre</option>
                                    {genres.map(genre => (
                                        <option key={genre} value={genre}>{genre}</option>
                                    ))}
                                </select>
                                {errors.genre && <span className={styles.errorText}>{errors.genre}</span>}
                            </div>

                            <div className={styles.inputGroup}>
                                <label htmlFor="language">Language *</label>
                                <select
                                    id="language"
                                    value={formData.language || 'ENGLISH'}
                                    onChange={(e) => handleInputChange('language', e.target.value as Language)}
                                >
                                    {languages.map(language => (
                                        <option key={language} value={language}>
                                            {language.charAt(0) + language.slice(1).toLowerCase()}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        <div className={styles.formRow}>
                            <div className={styles.inputGroup}>
                                <label htmlFor="age_group">Age Group *</label>
                                <select
                                    id="age_group"
                                    value={formData.age_group || 'ADULT'}
                                    onChange={(e) => handleInputChange('age_group', e.target.value as AgeGroup)}
                                >
                                    {ageGroups.map(ageGroup => (
                                        <option key={ageGroup} value={ageGroup}>
                                            {ageGroup.charAt(0) + ageGroup.slice(1).toLowerCase()}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className={styles.inputGroup}>
                                <label htmlFor="publication_date">Publication Date *</label>
                                <input
                                    id="publication_date"
                                    type="date"
                                    value={formData.publication_date || ''}
                                    onChange={(e) => handleInputChange('publication_date', e.target.value)}
                                    className={errors.publication_date ? styles.errorInput : ''}
                                    max={new Date().toISOString().split('T')[0]}
                                />
                                {errors.publication_date && <span className={styles.errorText}>{errors.publication_date}</span>}
                            </div>
                        </div>

                        <div className={styles.formRow}>
                            <div className={styles.inputGroup}>
                                <label htmlFor="price">Price ($) *</label>
                                <input
                                    id="price"
                                    type="number"
                                    min="0.01"
                                    step="0.01"
                                    value={formData.price || ''}
                                    onChange={(e) => handleInputChange('price', parseFloat(e.target.value) || 0)}
                                    className={errors.price ? styles.errorInput : ''}
                                    placeholder="0.00"
                                />
                                {errors.price && <span className={styles.errorText}>{errors.price}</span>}
                            </div>

                            <div className={styles.inputGroup}>
                                <label htmlFor="pages">Pages *</label>
                                <input
                                    id="pages"
                                    type="number"
                                    min="1"
                                    value={formData.pages || ''}
                                    onChange={(e) => handleInputChange('pages', parseInt(e.target.value) || 0)}
                                    className={errors.pages ? styles.errorInput : ''}
                                    placeholder="Number of pages"
                                />
                                {errors.pages && <span className={styles.errorText}>{errors.pages}</span>}
                            </div>
                        </div>

                        <div className={styles.inputGroup}>
                            <label htmlFor="characteristics">Characteristics *</label>
                            <textarea
                                id="characteristics"
                                value={formData.characteristics || ''}
                                onChange={(e) => handleInputChange('characteristics', e.target.value)}
                                className={errors.characteristics ? styles.errorInput : ''}
                                placeholder="Enter book characteristics (e.g., plot twists, colorful illustrations)"
                                rows={3}
                            />
                            {errors.characteristics && <span className={styles.errorText}>{errors.characteristics}</span>}
                        </div>

                        <div className={styles.inputGroup}>
                            <label htmlFor="description">Description *</label>
                            <textarea
                                id="description"
                                value={formData.description || ''}
                                onChange={(e) => handleInputChange('description', e.target.value)}
                                className={errors.description ? styles.errorInput : ''}
                                placeholder="Enter detailed book description"
                                rows={4}
                            />
                            {errors.description && <span className={styles.errorText}>{errors.description}</span>}
                        </div>
                    </div>

                    <div className={styles.formActions}>
                        <AuthorizationButton type='cancel' onClick={onCancel} />
                        <AuthorizationButton type='submit' />
                    </div>
                </form>
            </div>
        </>
    );
};