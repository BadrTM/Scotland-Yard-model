package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.*;

import javax.annotation.Nonnull;

import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

    private static final class MyGameState implements GameState {
        private final GameSetup setup;
        private ImmutableSet<Piece> remaining;
        private final ImmutableList<LogEntry> log;
        private Player mrX;
        private final List<Player> detectives;
        private final List<Player> Players = new ArrayList<>();
        private final HashSet<Move> hashMoves = new HashSet<>();

        private MyGameState(final GameSetup setup,
                            final ImmutableSet<Piece> remaining,
                            final ImmutableList<LogEntry> log,
                            final Player mrX,
                            final List<Player> detectives) {
            this.setup = setup;
            this.remaining = remaining;
            this.log = log;
            this.mrX = mrX;
            this.detectives = detectives;
            this.Players.addAll(this.detectives);
            this.Players.add(this.mrX);
            ruleChecks();
        }

        // Checking game state obeys game rules
        private void ruleChecks() {
            // Check if there is no MrX in-game
            if (!this.mrX.isMrX()) {
                throw new IllegalArgumentException("No MRX");
            }
            // Check if a detective piece == MrX (2+ MrX's in the game)
            if (this.detectives.stream().anyMatch(Player::isMrX)) {
                throw new IllegalArgumentException("2+ MrX's in the game FIRST");
            }
            // Check for empty moves or graph
            if (this.setup.moves.isEmpty() || this.setup.graph.nodes().isEmpty()) {
                throw new IllegalArgumentException("Empty Setup");
            }
            // Check for detectives Piece Overlap
            if (this.detectives.stream().anyMatch(detective -> Collections.frequency(this.detectives, detective) > 1)) {
                throw new IllegalArgumentException("Detective Piece Overlap");
            }
            // Check for detectives Location Overlap
            if (this.detectives.stream().anyMatch(detective -> Collections.frequency(this.detectives.stream().map(Player::location).toList(), detective.location()) > 1)) {
                throw new IllegalArgumentException("Detective Location Overlap");
            }
            // Check if detectives have illegal tickets
            if (this.detectives.stream().anyMatch(detective -> detective.has(ScotlandYard.Ticket.SECRET) || detective.has(ScotlandYard.Ticket.DOUBLE))) {
                throw new IllegalArgumentException("Detective Holds Illegal Ticket");
            }
        }

        private static Set<Move.SingleMove> makeSingleMoves(GameSetup setup,
                                                            List<Player> detectives,
                                                            Player player,
                                                            int source) {
            Set<Move.SingleMove> moveSet = new HashSet<>();

            for (int destination : setup.graph.adjacentNodes(source)) {
                if (detectives.stream().noneMatch(detective -> detective.location() == destination)) {
                    for (ScotlandYard.Transport t : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {
                        if (player.has(t.requiredTicket())) {
                            moveSet.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));
                        }
                    }
                    if (player.has(ScotlandYard.Ticket.SECRET)) {
                        moveSet.add((new Move.SingleMove(player.piece(), source, ScotlandYard.Ticket.SECRET, destination)));
                    }
                }
            }
            return moveSet;
        }

        private static Set<Move.DoubleMove> makeDoubleMoves(GameSetup setup,
                                                            List<Player> detectives,
                                                            Player MrX,
                                                            int source) {
            Set<Move.DoubleMove> moveSet = new HashSet<>();

            for (int destination1 : setup.graph.adjacentNodes(source)) {
                if (detectives.stream().noneMatch(detective -> detective.location() == destination1)) {
                    for (ScotlandYard.Transport firstTicket : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination1, ImmutableSet.of()))) {
                        for (int destination2 : setup.graph.adjacentNodes(destination1)) {
                            if (detectives.stream().noneMatch(detective -> detective.location() == destination2)) {
                                for (ScotlandYard.Transport secondTicket : Objects.requireNonNull(setup.graph.edgeValueOrDefault(destination1, destination2, ImmutableSet.of()))) {
                                    // DoubleMove uses 2 tickets of the same type (No Secret Ticket)
                                    if ((firstTicket == secondTicket) && (MrX.hasAtLeast(firstTicket.requiredTicket(), 2))) {
                                        moveSet.add(new Move.DoubleMove(MrX.piece(), source, firstTicket.requiredTicket(), destination1, secondTicket.requiredTicket(), destination2));
                                    }
                                    // DoubleMove uses 2 tickets of different types (No Secret Ticket)
                                    else if ((firstTicket != secondTicket) && MrX.has(firstTicket.requiredTicket()) && MrX.has(secondTicket.requiredTicket())) {
                                        moveSet.add(new Move.DoubleMove(MrX.piece(), source, firstTicket.requiredTicket(), destination1, secondTicket.requiredTicket(), destination2));
                                    }
                                    // DoubleMove uses Secret ticket for 2nd move
                                    if ((MrX.has(firstTicket.requiredTicket())) && (MrX.has(ScotlandYard.Ticket.SECRET))) {
                                        moveSet.add(new Move.DoubleMove(MrX.piece(), source, firstTicket.requiredTicket(), destination1, ScotlandYard.Ticket.SECRET, destination2));
                                    }
                                    // DoubleMove uses Secret ticket for 1st move
                                    if ((MrX.has(secondTicket.requiredTicket())) && (MrX.has(ScotlandYard.Ticket.SECRET))) {
                                        moveSet.add(new Move.DoubleMove(MrX.piece(), source, ScotlandYard.Ticket.SECRET, destination1, secondTicket.requiredTicket(), destination2));
                                    }
                                    // DoubleMove uses 2 Secret tickets
                                    if (MrX.hasAtLeast(ScotlandYard.Ticket.SECRET, 2)) {
                                        moveSet.add(new Move.DoubleMove(MrX.piece(), source, ScotlandYard.Ticket.SECRET, destination1, ScotlandYard.Ticket.SECRET, destination2));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return moveSet;
        }

        private GameState moveMrX(List<Integer> destinationList, List<ScotlandYard.Ticket> usedTickets) {
            List<LogEntry> thisTurnLog = new ArrayList<>();
            List<Integer> revealTurns = new ArrayList<>();
            this.mrX = this.mrX.use(usedTickets);

            for (int i = this.log.size(); i < this.setup.moves.size(); i++) {
                if (this.setup.moves.get(i)) {
                    revealTurns.add(i + 1);
                }
            }

            // If MrX used a DoubleMove (Add 2 log entries + move MrX to destination2)
            if (usedTickets.stream().anyMatch(ticket -> ticket == ScotlandYard.Ticket.DOUBLE)) {
                // Add 2 log entries of the type specified in this.setup.moves
                if ((revealTurns.contains(this.log.size() + 1)) && ((revealTurns.contains(this.log.size() + 2)))) {
                    thisTurnLog.add(LogEntry.reveal(usedTickets.get(0), destinationList.get(0)));
                    thisTurnLog.add(LogEntry.reveal(usedTickets.get(1), destinationList.get(1)));
                } else if (revealTurns.contains(this.log.size() + 1)) {
                    thisTurnLog.add(LogEntry.reveal(usedTickets.get(0), destinationList.get(0)));
                    thisTurnLog.add(LogEntry.hidden(usedTickets.get(1)));
                } else if (revealTurns.contains(this.log.size() + 2)) {
                    thisTurnLog.add(LogEntry.hidden(usedTickets.get(0)));
                    thisTurnLog.add(LogEntry.reveal(usedTickets.get(1), destinationList.get(1)));
                } else {
                    thisTurnLog.add(LogEntry.hidden(usedTickets.get(0)));
                    thisTurnLog.add(LogEntry.hidden(usedTickets.get(1)));
                }
                this.mrX = this.mrX.at(destinationList.get(1));
            }
            // SingleMove
            else {
                // If log entry should be revealed this turn (SingleMove), add revealed LogEntry, else hidden LogEntry
                if (revealTurns.contains(this.log.size() + 1)) {
                    thisTurnLog.add(LogEntry.reveal(usedTickets.get(0), destinationList.get(0)));
                } else {
                    thisTurnLog.add(LogEntry.hidden(usedTickets.get(0)));
                }
                this.mrX = this.mrX.at(destinationList.get(0));
            }

            return new MyGameState(this.setup,
                    ImmutableSet.copyOf(this.detectives.stream().map(Player::piece).toList()),
                    ImmutableList.copyOf(Streams.concat(this.log.stream(), thisTurnLog.stream()).collect(Collectors.toList())),
                    this.mrX,
                    this.detectives);
        }

        private GameState moveDetective(List<Integer> destinationList, Move move, ImmutableSet<Move> moves) {
            List<Player> newDets = new ArrayList<>();

            for (Player detective : this.detectives) {
                if (detective.piece() == move.commencedBy()) {
                    detective = detective.at(destinationList.get(0)); // Move detective
                    newDets.add(detective.use(move.tickets())); // Subtract ticket + add to updated detectives list
                    this.mrX = this.mrX.give(move.tickets()); // Give used ticket to MrX
                } else {
                    newDets.add(detective);
                }
            }

            this.remaining = ImmutableSet.copyOf(moves.stream()
                    .map(Move::commencedBy)
                    .filter(movingPiece -> movingPiece != move.commencedBy())
                    .collect(Collectors.toSet()));

            if (this.remaining.isEmpty()) {
                return new MyGameState(this.setup,
                        ImmutableSet.of(this.mrX.piece()),
                        this.log,
                        this.mrX,
                        newDets);
            } else {
                return new MyGameState(this.setup,
                        this.remaining,
                        this.log,
                        this.mrX,
                        newDets);
            }
        }

        @Nonnull
        @Override
        public GameState advance(Move move) {
            ImmutableSet<Move> moves = getAvailableMoves();

            if (!moves.contains(move)) {
                throw new IllegalArgumentException("Illegal move: " + move);
            }

            List<Integer> destinationList = move.accept(new Move.Visitor<>() {
                @Override
                public List<Integer> visit(Move.SingleMove move) {
                    return List.of(move.destination);
                }

                @Override
                public List<Integer> visit(Move.DoubleMove move) {
                    return List.of(move.destination1, move.destination2);
                }
            });

            // MrX moves
            if (move.commencedBy() == this.mrX.piece()) {
                List<ScotlandYard.Ticket> usedTickets = StreamSupport.stream(move.tickets().spliterator(), false).toList();
                return moveMrX(destinationList, usedTickets);
            }
            // A detective moves
            else {
                return moveDetective(destinationList, move, moves);
            }
        }

        @Nonnull
        @Override
        public GameSetup getSetup() {
            return this.setup;
        }

        @Nonnull
        @Override
        public ImmutableSet<Piece> getPlayers() {
            return ImmutableSet.copyOf(this.Players.stream()
                    .map(Player::piece)
                    .collect(Collectors.toSet()));
        }

        @Nonnull
        @Override
        public Optional<Integer> getDetectiveLocation(Piece.Detective detectivePiece) {
            // Get detective location by checking matching piece from detectives list, then
            // calling the .location() method and passing
            try {
                Integer detLocation = this.detectives.stream()
                        .filter(detective -> detective.piece() == detectivePiece)
                        .toList()
                        .get(0)
                        .location();
                return Optional.of(detLocation);
            } catch (ArrayIndexOutOfBoundsException e) {
                return Optional.empty();
            }
        }

        @Nonnull
        @Override
        public Optional<TicketBoard> getPlayerTickets(Piece piece) {
            if (this.Players.stream().anyMatch(player -> player.piece() == piece)) {
                TicketBoard playerTicket = ticket -> this.Players.stream()
                        .filter(player -> player.piece() == piece)
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
            return this.log;
        }

        @Nonnull
        @Override
        public ImmutableSet<Piece> getWinner() {
            // All detective have no more moves or TravelLog is full, MrX wins
            if ((this.remaining.contains(this.mrX.piece()))
                    && ((this.detectives.stream().allMatch(detective -> detective.tickets().values().stream().allMatch(ticketTypeAvailable -> ticketTypeAvailable == 0)))
                    || (this.log.size() == this.setup.moves.size()))) {
                return ImmutableSet.of(this.mrX.piece());
            }
            // Detective finds MrX or MrX has no more moves, Detectives win
            else if ((this.detectives.stream().anyMatch(detective -> detective.location() == this.mrX.location()))
                    || ((this.remaining.contains(this.mrX.piece()))
                    && (getAvailableMoves().isEmpty()))) {
                return ImmutableSet.copyOf(this.detectives.stream().map(Player::piece).collect(Collectors.toSet()));
            }
            // Still no winner
            else {
                return ImmutableSet.of();
            }
        }

        @Nonnull
        @Override
        public ImmutableSet<Move> getAvailableMoves() {
            // No available moves if detectives caught mrX
            if (this.detectives.stream().anyMatch(detective -> detective.location() == this.mrX.location())
                    || this.detectives.stream().allMatch(detective -> detective.tickets().values().stream().allMatch(y -> y == 0))) {
                return ImmutableSet.of();
            }
            // Get mrX's available moves
            else if (this.remaining.contains(this.mrX.piece())) {
                if (getMrXTravelLog().size() == getSetup().moves.size()) {
                    return ImmutableSet.of();
                }
                this.hashMoves.addAll(makeSingleMoves(this.setup, this.detectives, this.mrX, this.mrX.location()));
                if ((this.mrX.has(ScotlandYard.Ticket.DOUBLE)) && (this.setup.moves.size() - this.log.size() >= 2)) {
                    this.hashMoves.addAll(makeDoubleMoves(this.setup, this.detectives, this.mrX, this.mrX.location()));
                }
            }
            // Get detectives available moves
            else {
                for (Player detective : this.detectives) {
                    if (this.remaining.stream().anyMatch(remainingPLayer -> remainingPLayer == detective.piece())) {
                        this.hashMoves.addAll((makeSingleMoves(this.setup, this.detectives, detective, detective.location())));
                    }
                }
            }
            return ImmutableSet.copyOf(this.hashMoves);
        }
    }

    @Nonnull
    @Override
    public GameState build(GameSetup setup,
                           Player mrX,
                           ImmutableList<Player> detectives) {
        return new MyGameState(setup, ImmutableSet.of(Piece.MrX.MRX), ImmutableList.of(), mrX, detectives);
    }
}
