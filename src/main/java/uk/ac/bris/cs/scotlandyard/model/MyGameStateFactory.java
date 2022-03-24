package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import org.controlsfx.control.ListSelectionView;
import org.w3c.dom.Node;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.*;
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

		List<Player> Players = new ArrayList<>(){ {addAll(detectives); add(mrX);} };
		List<Integer> detsLocations = detectives.stream()
												.map(Player::location)
												.toList();

		// Check if there is no MrX
		if (!mrX.isMrX()) {
			System.out.println("No MRX");
			throw new IllegalArgumentException();
		}

		// Check if any detective is also MrX (aka 2+ MrX's in the game) (JOIN WITH TOP IF LATER)
		if (detectives.stream().anyMatch(Player::isMrX)) {
			System.out.println("2+ MrX's in the game FIRST");
			throw new IllegalArgumentException();
		}

		// Check for empty moves or graph
		if (setup.moves.isEmpty() || setup.graph.nodes().isEmpty()) {
			System.out.println("Empty Setup");
			throw new IllegalArgumentException();
		}

		// Check if any 2 detectives are assigned the same location/piece (Overlap)
		if (detectives.stream().anyMatch(x -> ((Collections.frequency(detectives, x) > 1) || (Collections.frequency(detsLocations, x.location()) > 1)))) {
			System.out.println("Detective Location/Piece Overlap");
			throw new IllegalArgumentException();
		}

		// Check if detectives have illegal tickets
		if (detectives.stream().anyMatch(x -> x.has(ScotlandYard.Ticket.SECRET) || x.has(ScotlandYard.Ticket.DOUBLE))) {
			System.out.println("Detective Holds Illegal Ticket");
			throw new IllegalArgumentException();
		}

		// consider that the class can be private and final
		GameState MyGameState = new GameState() {
			@Nonnull
			@Override
			public GameState advance(Move move) {
				GameState x = build(getSetup(), mrX, detectives);
				return x;
			}

			@Nonnull
			@Override
			public GameSetup getSetup() {
				return setup;
			}

			@Nonnull
			@Override
			public ImmutableSet<Piece> getPlayers() {
				return ImmutableSet.copyOf(Players.stream()
													.map(Player::piece)
													.collect(Collectors.toSet()));
			}

			@Nonnull
			@Override
			public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
				// Get detective location by checking matching piece from detectives list, then
				// calling the .location() method and passing
				try {
					Integer detLocation = detectives.stream()
													.filter(x -> x.piece() == detective)
													.toList()
													.get(0)
													.location();
					return Optional.of(detLocation);
				} catch (ArrayIndexOutOfBoundsException e){
					return Optional.empty();
				}
			}

			@Nonnull
			@Override
			public Optional<TicketBoard> getPlayerTickets(Piece piece) {
				if (Players.stream().anyMatch(x -> x.piece() == piece)) {
					TicketBoard playerTicket = ticket -> Players.stream()
																.filter(x -> x.piece() == piece)
																.toList()
																.get(0)
																.tickets()
																.get(ticket);
					return Optional.of(playerTicket);
				} else {
					return Optional.empty();
				}
			}

			@Nonnull
			@Override
			public ImmutableList<LogEntry> getMrXTravelLog() {
				return null;
			}

			@Nonnull
			@Override
			public ImmutableSet<Piece> getWinner() {
				ImmutableSet<Piece> gameWinner = ImmutableSet.of();
				return gameWinner;
			}

			@Nonnull
			@Override
			public ImmutableSet<Move> getAvailableMoves() {

				List<Move> moveList = new ArrayList<>();

				//CHECK VISITOR PATTERN

				for (Player detective: detectives){
					// Current piece
					Piece curPiece = detective.piece();

					// Piece location
					int curLocation = detective.location();

					// Ticket values for TAXI, BUS and UNDERGROUND respectively for each detective piece
					ImmutableList<Integer> xTicketLeft = detective.tickets().values().asList();

					// TAXI ticket loop
					for (int i = 0; i < xTicketLeft.get(0); i++) {
						moveList.add(new Move.SingleMove(curPiece, curLocation, ScotlandYard.Ticket.TAXI, i));
					}
					// BUS ticket loop
					for (int i = 0; i < xTicketLeft.get(1); i++) {
						moveList.add(new Move.SingleMove(curPiece, curLocation, ScotlandYard.Ticket.BUS, i));
					}
					// UNDERGROUND ticket loop
					for (int i = 0; i < xTicketLeft.get(2); i++) {
						moveList.add(new Move.SingleMove(curPiece, curLocation, ScotlandYard.Ticket.UNDERGROUND, i));
					}
				}
				return ImmutableSet.copyOf(moveList);
			}
		};

		return MyGameState;
	}

}
