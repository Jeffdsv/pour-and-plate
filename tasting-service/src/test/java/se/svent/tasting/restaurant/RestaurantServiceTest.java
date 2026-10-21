package se.svent.tasting.restaurant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import se.svent.tasting.user.AppUser;
import se.svent.tasting.user.AppUserRepository;
import se.svent.tasting.user.Role;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// TODO: mockito currently attaches itself as a java agent at runtime and prints a warning
// newer jdk versions will block that by default, so the fix is to load mockito as an agent
// in the maven surefire plugin in pom.xml, planned for when the jenkins pipeline is set up

// tests the logic in RestaurantService with fake repositories, no database
class RestaurantServiceTest {

    private static final BigDecimal LAT = new BigDecimal("59.33");
    private static final BigDecimal LON = new BigDecimal("18.06");

    // the logged in user in these tests
    private static final String USERNAME = "anna";
    private final AppUser anna = new AppUser(USERNAME, "Anna", "hash", Role.FRIEND);

    private RestaurantRepository repository;
    private RestaurantService service;

    @BeforeEach
    void setUp() {
        repository = mock(RestaurantRepository.class);

        // the fake user repository always finds anna
        AppUserRepository users = mock(AppUserRepository.class);
        when(users.findByUsername(USERNAME)).thenReturn(Optional.of(anna));

        when(repository.save(any(Restaurant.class))).thenAnswer(call -> call.getArgument(0));
        service = new RestaurantService(repository, users);
    }

    // a place i have not saved before becomes a new restaurant owned by me
    @Test
    void save_newPlace_createsRestaurantOwnedByMe() {
        when(repository.findByUserIdAndPlaceId(any(), eq("node/1"))).thenReturn(Optional.empty());

        RestaurantResponse response = service.save(USERNAME, wantToVisitRequest("node/1"));

        // an ArgumentCaptor grabs the object that was passed to save, so we can look inside it
        ArgumentCaptor<Restaurant> saved = ArgumentCaptor.forClass(Restaurant.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getUser()).isSameAs(anna);
        assertThat(response.status()).isEqualTo(RestaurantStatus.WANT_TO_VISIT);
    }

    // my same place again should update my existing review, not make a duplicate
    @Test
    void save_myExistingPlace_updatesIt() {
        Restaurant existing = Restaurant.wantToVisit(anna, "Ekstedt", LAT, LON);
        when(repository.findByUserIdAndPlaceId(any(), eq("node/1"))).thenReturn(Optional.of(existing));

        RestaurantResponse response = service.save(USERNAME, visitedRequest("node/1", 9, 8, 6));

        verify(repository).save(existing);
        assertThat(existing.getStatus()).isEqualTo(RestaurantStatus.VISITED);
        assertThat(response.myScore()).isEqualTo(7.7);
    }

    // an id that doesnt exist should give our not found error
    @Test
    void findById_unknownId_throwsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(USERNAME, 99L))
                .isInstanceOf(RestaurantNotFoundException.class)
                .hasMessageContaining("99");
    }

    // deleting an unknown id should fail and never call delete on the repository
    @Test
    void delete_unknownId_doesNotDeleteAnything() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(USERNAME, 99L))
                .isInstanceOf(RestaurantNotFoundException.class);

        verify(repository, never()).delete(any());
    }

    // helpers that build test requests so the tests above stay short

    private SaveRestaurantRequest wantToVisitRequest(String placeId) {
        return new SaveRestaurantRequest(placeId, "Ekstedt", null, "Stockholm", LAT, LON,
                RestaurantStatus.WANT_TO_VISIT, null, null, null, null, null, null);
    }

    private SaveRestaurantRequest visitedRequest(String placeId, int food, int atmosphere, int value) {
        return new SaveRestaurantRequest(placeId, "Ekstedt", null, "Stockholm", LAT, LON,
                RestaurantStatus.VISITED, food, atmosphere, value, null, null, null);
    }
}