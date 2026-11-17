package guru.springframework.jdbc.dao;

import guru.springframework.jdbc.domain.Book;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Statement;

@Component
public class BookDaoImpl implements BookDao {
    private final JdbcTemplate jdbcTemplate;

    public BookDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Book getById(Long id) {
        return jdbcTemplate.queryForObject("SELECT * FROM book where id = ?", getBookMapper(), id);
    }

    @Override
    public Book findBookByTitle(String title) {
        return jdbcTemplate.queryForObject("SELECT * FROM book where title = ?", getBookMapper(), title);
    }

    @Override
    public Book saveNewBook(Book book) {
        KeyHolder keyHolder = new GeneratedKeyHolder(); // Holder for generated id key after insert
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO book (isbn, publisher, title, author_id) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, book.getIsbn());
            statement.setString(2, book.getPublisher());
            statement.setString(3, book.getTitle());
            if (book.getAuthorId() != null) {
                statement.setLong(4, book.getAuthorId());
            } else {
                statement.setNull(4, -5);
            }

            return statement;
        }, keyHolder);

        Long createdId = keyHolder.getKey().longValue(); // extracting the id
        return this.getById(createdId);
    }

    @Override
    public Book updateBook(Book book) {
        jdbcTemplate.update("UPDATE book set isbn = ?, publisher = ?, title = ?, author_id = ? where id = ?",
                book.getIsbn(), book.getPublisher(), book.getTitle(), book.getAuthorId(), book.getId());

        return this.getById(book.getId());
    }

    @Override
    public void deleteBookById(Long id) {
        jdbcTemplate.update("DELETE from book where id = ?", id);
    }

    private BookMapper getBookMapper() {
        return new BookMapper();
    }
}
