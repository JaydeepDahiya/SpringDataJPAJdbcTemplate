package guru.springframework.jdbc.dao;

import guru.springframework.jdbc.domain.Author;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.List;

/**
 * Created by jt on 8/22/21.
 */
@Component
public class AuthorDaoImpl implements AuthorDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public AuthorDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public Author getById(Long id) {
        return jdbcTemplate.queryForObject("SELECT * FROM author where id = ?", getRowMapper(), id);
    }

    @Override
    public Author findAuthorByName(String firstName, String lastName) {
        return jdbcTemplate.queryForObject("SELECT * FROM author WHERE first_name = ? and last_name = ?",
                getRowMapper(),
                firstName, lastName);
    }

    @Override
    public Author saveNewAuthor(Author author) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO author (first_name, last_name) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, author.getFirstName());
            statement.setString(2, author.getLastName());
            return statement;
        }, keyHolder);

        Long createdId = keyHolder.getKey().longValue();
        return this.getById(createdId);
    }

    // Named Parameters can be used to pass in values as key value pair(map). It is useful when many parameters are present
    // Sample method to get list of authors with specific firstname and lastname
    public List<Author> findAuthorsByName(String firstName, String lastName) {
        String sql = "SELECT * FROM author WHERE first_name = :firstName, last_name = :lastName";
        // Bind each value to its corresponding named SQL parameter via MapSqlParameterSource
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("firstName", firstName)
                .addValue("lastName", lastName);
        return namedParameterJdbcTemplate.query(sql, parameters, getRowMapper()); // can be used for select query and get a list of accounts
    }

    // Named Parameters can be used to pass in values as key value pair(map). It is useful when many parameters are present
    // Sample method to update a specific id author's firstname and lastname and then return the updated author
    public Author updateAuthorWithNamedParameters(Author author) {
        String sql = "UPDATE author SET first_name = :firstName, last_name = :lastName WHERE id = :id";
        // Bind each value to its corresponding named SQL parameter via MapSqlParameterSource
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("firstName", author.getFirstName())
                .addValue("lastName", author.getLastName())
                .addValue("id", author.getId());
        // passing the MapSqlParameterSource to NamedParameterJdbcTemplate
        namedParameterJdbcTemplate.update(sql, parameters);
        return this.getById(author.getId());
    }

    @Override
    public Author updateAuthor(Author author) {
        jdbcTemplate.update("UPDATE author SET first_name = ?, last_name = ? WHERE id = ?",
                author.getFirstName(), author.getLastName(), author.getId());

        return this.getById(author.getId());
    }

    @Override
    public void deleteAuthorById(Long id) {
        jdbcTemplate.update("DELETE FROM author WHERE id = ?", id);
    }

    private RowMapper<Author> getRowMapper() {
        return new AuthorMapper();
    }

}







