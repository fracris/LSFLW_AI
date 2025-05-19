% ================================
% PROGRAMMA 1: SELEZIONE STRATEGIA
% ================================

ai_player(2).

% Livello di difficoltà: medium o hard
difficulty(hard).  % Cambiare in difficulty(hard) per livello difficile

system(0).
owner(0,0).
ships(0,320).
production(0,2).
system(1).
owner(1,2).
ships(1,160).
production(1,1).
system(2).
neutral(2).
ships(2,0).
production(2,1).
system(3).
neutral(3).
ships(3,0).
production(3,2).
system(4).
neutral(4).
ships(4,0).
production(4,1).
system(5).
neutral(5).
ships(5,0).
production(5,1).
system(6).
neutral(6).
ships(6,0).
production(6,2).
system(7).
neutral(7).
ships(7,0).
production(7,1).
system(8).
neutral(8).
ships(8,0).
production(8,1).
system(9).
neutral(9).
ships(9,0).
production(9,2).
system(10).
neutral(10).
ships(10,0).
production(10,1).
system(11).
neutral(11).
ships(11,0).
production(11,1).
system(12).
neutral(12).
ships(12,0).
production(12,1).
system(13).
neutral(13).
ships(13,0).
production(13,1).
system(14).
neutral(14).
ships(14,0).
production(14,1).
system(15).
neutral(15).
ships(15,0).
production(15,1).
system(16).
neutral(16).
ships(16,0).
production(16,1).
system(17).
neutral(17).
ships(17,0).
production(17,1).
system(18).
neutral(18).
ships(18,0).
production(18,2).
system(19).
owner(19,1).
ships(19,160).
production(19,1).
connected(0,1).
connected(0,3).
connected(1,3).
connected(2,4).
connected(3,4).
connected(3,6).
connected(4,5).
connected(4,7).
connected(4,9).
connected(5,9).
connected(6,7).
connected(7,10).
connected(8,11).
connected(8,12).
connected(8,13).
connected(9,13).
connected(10,11).
connected(10,14).
connected(11,15).
connected(12,16).
connected(13,16).
connected(13,19).
connected(14,17).
connected(15,16).
connected(15,17).
connected(15,18).
connected(16,18).
connected(16,19).

% Dati storici per confronto
previous_my_system_count(1).
previous_enemy_system_count(1).
previous_neutral_system_count(8).
previous_my_ships_total(110).
previous_enemy_ships_total(110).
previous_enemy_ships_total(0,0).
previous_enemy_ships_total(1,110).

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
