package book.store.repository.impl;

import book.store.exception.DataProcessingException;
import book.store.exception.EntityNotFoundException;
import book.store.model.Book;
import book.store.repository.BookRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class BookRepositoryImpl implements BookRepository {
    private final EntityManagerFactory managerFactory;

    @Override
    public Book save(Book book) {
        EntityTransaction transaction = null;

        try (EntityManager manager = managerFactory.createEntityManager()) {
            transaction = manager.getTransaction();
            transaction.begin();
            manager.persist(book);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new DataProcessingException("Can't save " + book + " to DB", e);
        }
        return book;
    }

    @Override
    public Optional<Book> findBookById(Long id) {
        try (EntityManager manager = managerFactory.createEntityManager()) {
            return Optional.ofNullable(manager.find(Book.class, id));
        } catch (Exception e) {
            throw new DataProcessingException("Can't find book from DB by id: " + id, e);
        }
    }

    @Override
    public List<Book> findAll() {
        try (EntityManager manager = managerFactory.createEntityManager()) {
            return manager.createQuery("FROM Book ", Book.class).getResultList();
        } catch (Exception e) {
            throw new DataProcessingException("Can't find books from DB", e);
        }
    }
}
