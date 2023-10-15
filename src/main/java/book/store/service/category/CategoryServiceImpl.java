package book.store.service.category;

import book.store.dto.book.BookDtoWithoutCategoryIds;
import book.store.dto.category.CategoryRequestDto;
import book.store.dto.category.CategoryResponseDto;
import book.store.exception.EntityNotFoundException;
import book.store.mapper.BookMapper;
import book.store.mapper.CategoryMapper;
import book.store.model.Category;
import book.store.repository.book.BookRepository;
import book.store.repository.category.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    public CategoryResponseDto save(CategoryRequestDto request) {
        Category category = categoryMapper.toEntity(request);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public CategoryResponseDto getById(Long id) {
        return categoryMapper.toDto(categoryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find category by id: " + id)
        ));
    }

    @Override
    public List<CategoryResponseDto> getAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryResponseDto update(Long id, CategoryRequestDto request) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    "It is not possible to update a category by id: " + id
            );
        }

        Category category = categoryMapper.toEntity(request);
        category.setId(id);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public List<BookDtoWithoutCategoryIds> getBooksByCategoryId(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Can't find category by id: " + id);
        }

        return bookRepository.findAllByCategoryId(id).stream()
                .map(bookMapper::toDtoWithoutCategories)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    "It is not possible to delete a category by id: " + id
            );
        }
        categoryRepository.deleteById(id);
    }
}
