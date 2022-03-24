package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.Optional;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	@Nonnull @Override public Model build(GameSetup setup,
	                                      Player mrX,
	                                      ImmutableList<Player> detectives) {
		// TODO

		Model gameModel = new Model() {
			@Nonnull
			@Override
			public Board getCurrentBoard() {
				Board currentBoard = new Board() {
					@Nonnull
					@Override
					public GameSetup getSetup() {
						return null;
					}

					@Nonnull
					@Override
					public ImmutableSet<Piece> getPlayers() {
						return null;
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
				return null;
			}

			@Override
			public void registerObserver(@Nonnull Observer observer) {

			}

			@Override
			public void unregisterObserver(@Nonnull Observer observer) {

			}

			@Nonnull
			@Override
			public ImmutableSet<Observer> getObservers() {
				return null;
			}

			@Override
			public void chooseMove(@Nonnull Move move) {

			}
		};
		throw new RuntimeException("Implement me!");
	}
}
