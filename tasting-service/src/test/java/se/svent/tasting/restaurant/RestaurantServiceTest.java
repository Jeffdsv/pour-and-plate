package se.svent.tasting.restaurant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// TODO: mockito currently attaches itself as a java agent at runtime and prints a warning
// newer jdk versions will block that by default, so the fix is to load mockito as an agent
// in the maven surefire plugin in pom.xml, planned for when the jenkins pipeline is set up


// test the logic in RestaurantService with a fake repository, no database
class RestaurantServiceTest {

    private static final BigDecimal LAT = new BigDecimal("59.33");
    private static final BigDecimal LON = new BigDecimal("18.06");

    private RestaurantRepository repository;
    private RestaurantService service;

    // runs before every test, so each test starts with a fresh fake
    // mock() creates a fake repository
    // save() on the fake just gives back the same restaurant it got
    @BeforeEach
    void setUp() {
        repository = mock(RestaurantRepository.class);

        when(repository.save(any(Restaurant.class))).thenAnswer(call -> call.getArgument(0));

        service = new RestaurantService(repository);
    }

    // a new place we havent saved before should create a new restaurant
    // osm = OpenStreetMap
    @Test
    void save_newOsmId_createsNewRestaurant() {
        when(repository.findByPlaceId("node/1")).thenReturn(Optional.empty());

        RestaurantResponse response = service.save(wantToVisitRequest("node/1"));

        verify(repository).save(any(Restaurant.class));
        assertThat(response.status()).isEqualTo(RestaurantStatus.WANT_TO_VISIT);
        assertThat(response.name()).isEqualTo("Ekstedt");
    }

    // the same place again should update the existing one, not make a duplicate
    @Test
    void save_existingOsmId_updatesExistingRestaurant() {
        Restaurant existing = Restaurant.wantToVisit("Ekstedt", LAT, LON);
        when(repository.findByPlaceId("node/1")).thenReturn(Optional.of(existing));

        RestaurantResponse response = service.save(visitedRequest("node/1", 9, 8, 6));

        verify(repository).save(existing);
        assertThat(existing.getStatus()).isEqualTo(RestaurantStatus.VISITED);
        assertThat(response.myScore()).isEqualTo(7.7);
    }

    // an id that doesnt exist should give not found error
    @Test
    void findById_unknownId_throwsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(RestaurantNotFoundException.class)
                .hasMessageContaining("99");
    }

    // deleting an unknown id should fail and never call delete on the repository
    @Test
    void delete_unknownId_doesNotDeleteAnything() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(RestaurantNotFoundException.class);

        // never() checks that delete was NOT called
        verify(repository, never()).delete(any());
    }

    // helpers that build test requests so the tests above stay short

    private SaveRestaurantRequest wantToVisitRequest(String osmId) {
        return new SaveRestaurantRequest(osmId, "Ekstedt", null, "Stockholm", LAT, LON,
                RestaurantStatus.WANT_TO_VISIT, null, null, null, null, null);
    }

    private SaveRestaurantRequest visitedRequest(String osmId, int food, int atmosphere, int value) {
        return new SaveRestaurantRequest(osmId, "Ekstedt", null, "Stockholm", LAT, LON,
                RestaurantStatus.VISITED, food, atmosphere, value, null, null);
    }
}