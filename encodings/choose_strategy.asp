% ================================
% PROGRAMMA 1: SELEZIONE STRATEGIA
% ================================

% Definizioni di base per i sistemi (invariate)
my_system(S) :- system(S), owner(S,P), ai_player(P).
enemy(P) :- system(S), owner(S,P), not ai_player(P), P != 0.
enemy_system(S,P) :- system(S), owner(S,P), not ai_player(P), P != 0.
neutral_system(S) :- system(S), neutral(S).

% Connessione non orientata
undirected_connected(M,S) :- connected(M,S).
undirected_connected(M,S) :- connected(S,M).

% Sistema di confine (vicino a nemici o neutrali)
border_system(S) :-
    my_system(S),
    undirected_connected(S,N),
    not my_system(N).

% Sistema con alta produzione
high_production_system(S) :-
    system(S),
    production(S,P),
    P >= 2.

% ================================
% ANALISI DELLO STATO DEL GIOCO PER GIOCATORE
% ================================
count_my_systems(S) :- my_system(S).
my_system_count(C) :- #count{S : count_my_systems(S)} = C.

count_enemy_systems(S,P) :- enemy_system(S,P).
enemy_system_count(P,C) :- enemy(P), #count{S : count_enemy_systems(S,P)} = C.

count_neutral_systems(S) :- neutral_system(S).
neutral_system_count(C) :- #count{S : count_neutral_systems(S)} = C.

sum_my_ships(Ships,S) :- my_system(S), ships(S,Ships).
my_ships_total(T) :- #sum{Ships,S : sum_my_ships(Ships,S)} = T.

sum_enemy_ships(Ships,S,P) :- enemy_system(S,P), ships(S,Ships).
enemy_ships_total(P,T) :- enemy(P), #sum{Ships,S : sum_enemy_ships(Ships,S,P)} = T.

% ================================
% VALUTAZIONE PER OGNI NEMICO
% ================================
outnumbered(P) :-
    enemy(P),
    my_ships_total(MyTotal),
    enemy_ships_total(P,EnemyTotal),
    MyTotal < EnemyTotal.

superior(P) :-
    enemy(P),
    my_ships_total(MyTotal),
    enemy_ships_total(P,EnemyTotal),
    MyTotal * 10 > EnemyTotal * 13.

enemy_weakened(P) :-
    enemy(P),
    enemy_ships_total(P, Current),
    previous_enemy_ships_total(P, Previous),
    Current < Previous.

enemy_strengthened(P) :-
    enemy(P),
    enemy_ships_total(P, Current),
    previous_enemy_ships_total(P, Previous),
    Current > Previous.

neutral_opportunity :- neutral_system_count(Count), Count > 0.

systems_gained :-
    my_system_count(Current),
    previous_my_system_count(Previous),
    Current > Previous.

systems_lost :-
    my_system_count(Current),
    previous_my_system_count(Previous),
    Current < Previous.

ships_increased :-
    my_ships_total(Current),
    previous_my_ships_total(Previous),
    Current > Previous.

ships_decreased :-
    my_ships_total(Current),
    previous_my_ships_total(Previous),
    Current < Previous.

% ================================
% CLASSIFICAZIONE DELLE STRATEGIE
% ================================
% Definizione delle categorie di strategie
enemy_oriented_strategy(aggressive).
enemy_oriented_strategy(defensive).

global_strategy(expansion).
global_strategy(consolidation).
% -- rimossa global_strategy(technological).

strategy(S) :- enemy_oriented_strategy(S).
strategy(S) :- global_strategy(S).

% ================================
% CANDIDATI STRATEGIA PER OGNI NEMICO
% ================================
candidate_aggressive(P) :- superior(P), enemy_weakened(P).
candidate_aggressive(P) :- superior(P), systems_gained.

candidate_defensive(P) :-
    enemy(P),
    outnumbered(P), not superior(P),
    my_ships_total(MT), enemy_ships_total(P,ET),
    MT*10 > ET*7.

candidate_defensive(P) :-
    enemy(P),
    enemy_strengthened(P), not superior(P),
    not systems_lost.

% ================================
% CANDIDATI STRATEGIA GLOBALE
% ================================
% candidate_technological.  % eliminato
candidate_expansion :- ships_increased, neutral_opportunity.
candidate_consolidation :- systems_gained, my_system_count(Count), Count > 2.

% Strategia di default se nessuna condizione è soddisfatta
candidate_expansion :- not candidate_consolidation.
% (technological non è più considerata)

% ================================
% CALCOLO DEI PESI DELLE STRATEGIE NEMICHE
% ================================
count_aggressive(P) :- candidate_aggressive(P).
strategy_weight(aggressive, W) :- #count{P : count_aggressive(P)} = W.

count_defensive(P) :- candidate_defensive(P).
strategy_weight(defensive, W) :- #count{P : count_defensive(P)} = W.

% ================================
% CALCOLO DEI PESI DELLE STRATEGIE GLOBALI
% ================================
strategy_weight(expansion, 1) :- candidate_expansion.
strategy_weight(expansion, 0) :- not candidate_expansion.

strategy_weight(consolidation, 1) :- candidate_consolidation.
strategy_weight(consolidation, 0) :- not candidate_consolidation.

% ================================
% POTENZIATORI PER STRATEGIE GLOBALI
% ================================
% boost_technological.  % eliminato
boost_expansion :- neutral_opportunity, ships_increased.
boost_consolidation :- systems_gained, ships_increased.

% ================================
% SCELTA DELLA STRATEGIA FINALE
% ================================
{ chosen_strategy(S) : strategy(S) } = 1 :- difficulty(medium).

{ chosen_strategy(S) : enemy_oriented_strategy(S) } = 1 :- difficulty(hard).
{ chosen_strategy(S) : global_strategy(S) } = 1 :- difficulty(hard).

% ================================
% PREFERENZE PER LA SCELTA
% ================================
:~ chosen_strategy(S), enemy_oriented_strategy(S), strategy_weight(S, W). [-W@2]

% :~ chosen_strategy(technological), boost_technological. [-3@1]  % eliminato
:~ chosen_strategy(expansion), boost_expansion. [-2@1]
:~ chosen_strategy(consolidation), boost_consolidation. [-2@1]

% Priorità addizionali per situazioni specifiche
:~ chosen_strategy(defensive), systems_lost. [-3@3]
:~ chosen_strategy(aggressive), enemy(P), superior(P). [-2@3]
% :~ chosen_strategy(technological), high_production_system(S), neutral_system(S). [-2@3]  % eliminato
:~ chosen_strategy(expansion), neutral_opportunity. [-1@3]

% ================================
% OUTPUT
% ================================
#show chosen_strategy/1.
#show my_system_count/1.
#show enemy_system_count/2.
#show neutral_system_count/1.
#show my_ships_total/1.
#show enemy_ships_total/2.
