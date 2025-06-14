package com.epam.rd.autocode.spring.project.testdata;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.BookItem;
import java.util.ArrayList;
import java.util.List;
import static com.epam.rd.autocode.spring.project.testdata.BookData.*;

public class BookItemData {

    public static List<BookItem> getBookItemEntities() {
        List<BookItem> bookItems = new ArrayList<>();
        List<Book> books = getBookEntities();

        BookItem item1 = new BookItem();
        item1.setId(1L);
        item1.setBook(books.get(0));
        item1.setQuantity(2);

        BookItem item2 = new BookItem();
        item2.setId(2L);
        item2.setBook(books.get(1));
        item2.setQuantity(1);

        BookItem item3 = new BookItem();
        item3.setId(3L);
        item3.setBook(books.get(2));
        item3.setQuantity(3);

        bookItems.add(item1);
        bookItems.add(item2);
        bookItems.add(item3);

        return bookItems;
    }

    public static List<BookItemDTO> getBookItemDTOs() {
        List<BookItemDTO> bookItemDTOs = new ArrayList<>();

        BookItemDTO dto1 = getBookItemDTO();

        BookItemDTO dto2 = new BookItemDTO();
        dto2.setBookName(BOOK_NAME_2);
        dto2.setQuantity(1);

        BookItemDTO dto3 = new BookItemDTO();
        dto3.setBookName(BOOK_NAME_3);
        dto3.setQuantity(3);

        bookItemDTOs.add(dto1);
        bookItemDTOs.add(dto2);
        bookItemDTOs.add(dto3);

        return bookItemDTOs;
    }

    public static BookItemDTO getBookItemDTO(){
        BookItemDTO dto = new BookItemDTO();
        dto.setBookName(BOOK_NAME_1);
        dto.setQuantity(2);
        return dto;
    }

    public static BookItem getBookItemEntity(){
        return getBookItemEntities().get(0);
    }
}
