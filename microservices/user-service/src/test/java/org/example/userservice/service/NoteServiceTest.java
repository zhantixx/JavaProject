package org.example.userservice.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.userservice.entity.Note;
import org.example.userservice.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteService noteService;

    @BeforeEach
    void setup() {
        // no-op
    }

    @Test
    void listUserNotes_delegatesToRepository() {
        Long userId = 1L;
        List<Note> expected = List.of(new Note(userId, "t1", "c1"), new Note(userId, "t2", "c2"));
        when(noteRepository.findAllByUserIdOrderByUpdatedAtDesc(userId)).thenReturn(expected);

        List<Note> actual = noteService.listUserNotes(userId);

        assertEquals(expected, actual);
        verify(noteRepository).findAllByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Test
    void listUserNotes_throwsOnNullUserId() {
        assertThrows(IllegalArgumentException.class, () -> noteService.listUserNotes(null));
        verifyNoInteractions(noteRepository);
    }

    @Test
    void create_savesWithNormalizedFields() {
        Long userId = 42L;
        String title = "  Hello  ";
        String content = "  World  ";

        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note created = noteService.create(userId, title, content);

        assertNotNull(created);
        assertEquals(userId, created.getUserId());

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(captor.capture());
        Note saved = captor.getValue();
        assertEquals("Hello", saved.getTitle());
        assertEquals("World", saved.getContent());
    }

    @Test
    void create_throwsOnNullUserId() {
        assertThrows(IllegalArgumentException.class, () -> noteService.create(null, "t", "c"));
        verifyNoInteractions(noteRepository);
    }

    @Test
    void getOwned_returnsNoteWhenExists() {
        Long userId = 5L;
        Long noteId = 10L;
        Note note = new Note(userId, "t", "c");
        when(noteRepository.findByIdAndUserId(noteId, userId)).thenReturn(Optional.of(note));

        Note actual = noteService.getOwned(userId, noteId);

        assertEquals(note, actual);
        verify(noteRepository).findByIdAndUserId(noteId, userId);
    }

    @Test
    void getOwned_throwsWhenNotFound() {
        Long userId = 5L;
        Long noteId = 999L;
        when(noteRepository.findByIdAndUserId(noteId, userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> noteService.getOwned(userId, noteId));
        verify(noteRepository).findByIdAndUserId(noteId, userId);
    }

    @Test
    void getOwned_throwsOnNullArgs() {
        assertThrows(IllegalArgumentException.class, () -> noteService.getOwned(null, 1L));
        assertThrows(IllegalArgumentException.class, () -> noteService.getOwned(1L, null));
        verifyNoInteractions(noteRepository);
    }

    @Test
    void update_changesProvidedFields() {
        Long userId = 2L;
        Long noteId = 7L;
        Note existing = new Note(userId, "old", "oldc");
        when(noteRepository.findByIdAndUserId(noteId, userId)).thenReturn(Optional.of(existing));
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note updated = noteService.update(userId, noteId, " new title ", " new content ");

        assertEquals("new title", updated.getTitle().trim());
        assertEquals("new content", updated.getContent().trim());
        verify(noteRepository).findByIdAndUserId(noteId, userId);
        verify(noteRepository).save(existing);
    }

    @Test
    void update_throwsWhenNotFound() {
        Long userId = 2L;
        Long noteId = 7L;
        when(noteRepository.findByIdAndUserId(noteId, userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> noteService.update(userId, noteId, "t", "c"));
        verify(noteRepository).findByIdAndUserId(noteId, userId);
        verify(noteRepository, never()).save(any());
    }

    @Test
    void update_throwsOnNullArgs() {
        assertThrows(IllegalArgumentException.class, () -> noteService.update(null, 1L, "t", "c"));
        assertThrows(IllegalArgumentException.class, () -> noteService.update(1L, null, "t", "c"));
        verifyNoInteractions(noteRepository);
    }

    @Test
    void delete_removesWhenOwned() {
        Long userId = 3L;
        Long noteId = 8L;
        when(noteRepository.deleteByIdAndUserId(noteId, userId)).thenReturn(1L);

        assertDoesNotThrow(() -> noteService.delete(userId, noteId));
        verify(noteRepository).deleteByIdAndUserId(noteId, userId);
    }

    @Test
    void delete_throwsWhenNotFound() {
        Long userId = 3L;
        Long noteId = 8L;
        when(noteRepository.deleteByIdAndUserId(noteId, userId)).thenReturn(0L);

        assertThrows(EntityNotFoundException.class, () -> noteService.delete(userId, noteId));
        verify(noteRepository).deleteByIdAndUserId(noteId, userId);
    }

    @Test
    void delete_throwsOnNullArgs() {
        assertThrows(IllegalArgumentException.class, () -> noteService.delete(null, 1L));
        assertThrows(IllegalArgumentException.class, () -> noteService.delete(1L, null));
        verifyNoInteractions(noteRepository);
    }
}
