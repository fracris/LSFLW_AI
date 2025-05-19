% ================================
% PROGRAMMA 1: SELEZIONE STRATEGIA
% ================================

% Definizioni di base per i sistemi
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
    P >= 2.  % Modificato da 5 a 2 in base ai dati forniti

% ================================
% ANALISI DELLO STATO DEL GIOCO PER GIOCATORE
% ================================

% Conta i sistemi posseduti dal giocatore IA
count_my_systems(S) :- my_system(S).
my_system_count(C) :- #count{S : count_my_systems(S)} = C.

% Conta i sistemi per ogni nemico
count_enemy_systems(S,P) :- enemy_system(S,P).
enemy_system_count(P,C) :- enemy(P), #count{S : count_enemy_systems(S,P)} = C.

% Conta i sistemi neutrali
count_neutral_systems(S) :- neutral_system(S).
neutral_system_count(C) :- #count{S : count_neutral_systems(S)} = C.

% Somma le navi dell'IA
sum_my_ships(Ships,S) :- my_system(S), ships(S,Ships).
my_ships_total(T) :- #sum{Ships,S : sum_my_ships(Ships,S)} = T.

% Somma le navi per ogni nemico
sum_enemy_ships(Ships,S,P) :- enemy_system(S,P), ships(S,Ships).
enemy_ships_total(P,T) :- enemy(P), #sum{Ships,S : sum_enemy_ships(Ships,S,P)} = T.

% ================================
% VALUTAZIONE PER OGNI NEMICO
% ================================

% Definisci le condizioni strategiche per ogni nemico
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

% Confronto con stato precedente per ogni nemico
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

% Opportunità di espansione
neutral_opportunity :- neutral_system_count(Count), Count > 0.

% Situazione generale
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
% CANDIDATI STRATEGIA PER OGNI NEMICO
% ================================

% Possibili strategie per ogni nemico
candidate_aggressive(P) :- superior(P), enemy_weakened(P).
candidate_aggressive(P) :- superior(P), systems_gained.

candidate_defensive(P) :-
    enemy(P),
    outnumbered(P), not superior(P),
    my_ships_total(MT), enemy_ships_total(P,ET),
    MT*10 > ET*7.  % Svantaggio limitato (almeno il 70% della forza nemica)

candidate_defensive(P) :-
    enemy(P),
    enemy_strengthened(P), not superior(P),
    not systems_lost.

candidate_technological(P) :-
    enemy(P),
    not outnumbered(P),
    not superior(P),
    neutral_opportunity,
    not systems_lost.

candidate_expansion(P) :-
    enemy(P),
    ships_increased,
    not systems_gained,
    neutral_opportunity.

candidate_consolidation(P) :-
    enemy(P),
    systems_gained,
    my_system_count(Count),
    Count > 2.

% Strategia di default per ogni nemico se nessuna condizione è soddisfatta
candidate_expansion(P) :-
    enemy(P),
    not candidate_aggressive(P),
    not candidate_defensive(P),
    not candidate_technological(P),
    not candidate_consolidation(P).

% ================================
% SCELTA DELLA STRATEGIA FINALE
% ================================

% Definisci le possibili strategie
strategy(aggressive).
strategy(defensive).
strategy(technological).
strategy(expansion).
strategy(consolidation).

% Seleziona una strategia se livello è medio, due se è difficile
{ chosen_strategy(S) : strategy(S) } = 1 :- difficulty(medium).
{ chosen_strategy(S) : strategy(S) } =  2 :- difficulty(hard).

% ================================
% CALCOLO DEI PESI DELLE STRATEGIE
% ================================

% Calcola il "peso" di ogni strategia in base al numero di nemici che la suggeriscono
count_aggressive(P) :- candidate_aggressive(P).
strategy_weight(aggressive, W) :- #count{P : count_aggressive(P)} = W.

count_defensive(P) :- candidate_defensive(P).
strategy_weight(defensive, W) :- #count{P : count_defensive(P)} = W.

count_technological(P) :- candidate_technological(P).
strategy_weight(technological, W) :- #count{P : count_technological(P)} = W.

count_expansion(P) :- candidate_expansion(P).
strategy_weight(expansion, W) :- #count{P : count_expansion(P)} = W.

count_consolidation(P) :- candidate_consolidation(P).
strategy_weight(consolidation, W) :- #count{P : count_consolidation(P)} = W.

has_weight(S) :- strategy_weight(S, W).

strategy_weight(S, 0) :- strategy(S), not has_weight(S).


% ================================
% PREFERENZE PER LA SCELTA
% ================================

% Preferisci le strategie con peso maggiore (più nemici la suggeriscono)
:~ chosen_strategy(S), strategy_weight(S, W). [-W@1]

% Priorità addizionali per situazioni specifiche
:~ chosen_strategy(defensive), systems_lost. [-3@2]
:~ chosen_strategy(aggressive), superior(P). [-2@2]
:~ chosen_strategy(consolidation), ships_increased. [-2@2]
:~ chosen_strategy(technological), high_production_system(S), neutral_system(S). [-2@2]
:~ chosen_strategy(expansion), neutral_opportunity. [-1@2]

% ================================
% OUTPUT
% ================================

#show chosen_strategy/1.
#show my_system_count/1.
#show enemy_system_count/2.
#show neutral_system_count/1.
#show my_ships_total/1.
#show enemy_ships_total/2.
