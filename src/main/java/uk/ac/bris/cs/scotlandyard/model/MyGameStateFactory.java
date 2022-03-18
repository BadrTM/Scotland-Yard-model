package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		// TODO

		// Check if there is no MrX
		if (!mrX.isMrX()) {
			System.out.println("No MRX");
			throw new IllegalArgumentException();
		}
		// Check for empty moves or graph
		if (setup.moves.isEmpty() || setup.graph.nodes().isEmpty()) {
			System.out.println("Empty Setup");
			throw new IllegalArgumentException();
		}

		List<Number> detectivesLocations = new ArrayList<>();
		List<Piece> detectivesPieces = new ArrayList<>();

		for (Player detective : detectives) {
			// Check if any detective is also MrX (aka 2+ MrX's in the game)
			if (detective.isMrX()) {
				System.out.println("2+ MrX's in the game");
				throw new IllegalArgumentException();
			}
			// Check if any 2 detectives are assigned the same location/piece (Overlap)
			if (detectivesLocations.stream().anyMatch(x -> (x.equals(detective.location()))) || detectivesPieces.stream().anyMatch(x -> x.equals(detective.piece()))) {
				System.out.println("Detective Location/Piece Overlap");
				throw new IllegalArgumentException();
			}
			// Check if detectives have illegal tickets
			if (detective.has(ScotlandYard.Ticket.SECRET) || detective.has(ScotlandYard.Ticket.DOUBLE)) {
				System.out.println("Detective Holds Illegal Ticket");
				throw new IllegalArgumentException();
			}
			// Add detective location and piece to respective list to check with next detectives in case
			// of overlap + get Players pieces(*can be improved)
			detectivesLocations.add(detective.location());
			detectivesPieces.add(detective.piece());
		}

		detectivesPieces.add(mrX.piece());
		ImmutableSet<Piece> playersList = ImmutableSet.copyOf(detectivesPieces);

		GameState FactoryGameState = new GameState() {
			@Nonnull
			@Override
			public GameState advance(Move move) {
				return null;
			}

			@Nonnull
			@Override
			public GameSetup getSetup() {
				return setup;
			}

			@Nonnull
			@Override
			public ImmutableSet<Piece> getPlayers() {
				return playersList;
			}

			@Nonnull
			@Override
			public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
				return Optional.empty();
			}

			@Nonnull
			@Override
			public Optional<TicketBoard> getPlayerTickets(Piece piece) {
				return Optional.empty();
			}

			@Nonnull
			@Override
			public ImmutableList<LogEntry> getMrXTravelLog() {
				return null;
			}

			@Nonnull
			@Override
			public ImmutableSet<Piece> getWinner() {
				return null;
			}

			@Nonnull
			@Override
			public ImmutableSet<Move> getAvailableMoves() {
				return null;
			}
		};

		return FactoryGameState;
	}

}
