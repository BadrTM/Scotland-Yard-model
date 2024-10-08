package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {
    @Nonnull
    @Override
    public Model build(GameSetup setup,
                       Player mrX,
                       ImmutableList<Player> detectives) {
        List<Model.Observer> observerList = new ArrayList<>();
        return new Model() {
            Board.GameState myGameState = new MyGameStateFactory().build(setup, mrX, detectives);

            @Nonnull
            @Override
            public Board getCurrentBoard() {

                return new Board() {
                    @Nonnull
                    @Override
                    public GameSetup getSetup() {
                        return myGameState.getSetup();
                    }

                    @Nonnull
                    @Override
                    public ImmutableSet<Piece> getPlayers() {
                        return myGameState.getPlayers();
                    }

                    @Nonnull
                    @Override
                    public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
                        return myGameState.getDetectiveLocation(detective);
                    }

                    @Nonnull
                    @Override
                    public Optional<TicketBoard> getPlayerTickets(Piece piece) {
                        return myGameState.getPlayerTickets(piece);
                    }

                    @Nonnull
                    @Override
                    public ImmutableList<LogEntry> getMrXTravelLog() {
                        return myGameState.getMrXTravelLog();
                    }

                    @Nonnull
                    @Override
                    public ImmutableSet<Piece> getWinner() {
                        return myGameState.getWinner();
                    }

                    @Nonnull
                    @Override
                    public ImmutableSet<Move> getAvailableMoves() {
                        return myGameState.getAvailableMoves();
                    }
                };
            }

            @Override
            public void registerObserver(@Nonnull Observer observer) {
                if (observer == null) {
                    throw new NullPointerException();
                } else if (!(observerList.contains(observer))) {
                    observerList.add(observer);
                } else {
                    throw new IllegalArgumentException();
                }
            }

            @Override
            public void unregisterObserver(@Nonnull Observer observer) {
                if (observer == null) {
                    throw new NullPointerException();
                } else if (!(observerList.contains(observer))) {
                    throw new IllegalArgumentException();
                } else {
                    observerList.remove(observer);
                }
            }

            @Nonnull
            @Override
            public ImmutableSet<Observer> getObservers() {
                return ImmutableSet.copyOf(observerList);
            }

            @Override
            public void chooseMove(@Nonnull Move move) {
                myGameState = myGameState.advance(move);
                Board currentBoard = getCurrentBoard();

                if (currentBoard.getWinner().isEmpty()) {
                    getObservers().forEach(x -> x.onModelChanged(currentBoard, Observer.Event.MOVE_MADE));
                } else {
                    getObservers().forEach(x -> x.onModelChanged(currentBoard, Observer.Event.GAME_OVER));
                }
            }
        };
    }
}
