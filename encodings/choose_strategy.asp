
% Identifica i sistemi controllati dall'IA
my_system(S) :- system(S), owner(S, P), ai_player(P).


% Identifica i giocatori nemici
enemy(P) :- system(S), owner(S, P), not ai_player(P).

% Identifica i sistemi nemici
enemy_system(S, P) :- system(S), owner(S, P), not ai_player(P).

% Connessioni non orientate tra sistemi
undirected_connected(M, S) :- connected(M, S).
undirected_connected(M, S) :- connected(S, M).

% Identifica i sistemi di confine che puo confinare sia con un neutrale e sia con un nemico
border_system(S) :- my_system(S), undirected_connected(S, N), not my_system(N).

% potremmo fare un border system sdoppiato uno per i nemici e uno per i neutrali


% Identifica i sistemi ad alta produzione
high_production_system(S) :- production(S, P), P > 2.

% Conta i sistemi controllati dall'IA
my_system_count(C) :- #count{S : my_system(S) } = C.

% Conta i sistemi nemici per ogni giocatore nemico
enemy_system_count(P, C) :- enemy(P), #count{S : enemy_system(S, P)} = C.

% Somma delle navi nei sistemi controllati dall'IA
sum_my_ships(Ships, S) :- my_system(S), ships(S, Ships).
my_ships_total(T) :- #sum{Ships, S : sum_my_ships(Ships, S)} = T.

% Somma delle navi nei sistemi nemici per ogni giocatore nemico
sum_enemy_ships(Ships, S, P) :- enemy_system(S, P), ships(S, Ships).
enemy_ships_total(P, T) :- enemy(P), #sum{Ships, S : sum_enemy_ships(Ships, S, P)} = T.




% Determina se un nemico si è rafforzato
enemy_strengthened(P) :- enemy(P), enemy_ships_total(P, Current), previous_enemy_ships_total(P, Previous), Current > Previous.

% Opportunità di espansione in presenza di sistemi neutrali
neutral_opportunity :- neutral_system(S).

% Verifica se l'IA ha guadagnato sistemi
systems_gained :- system_gained(S).

% Verifica se l'IA ha perso sistemi
systems_lost :- system_lost(S).

% Verifica se il numero di navi dell'IA è aumentato
ships_increased :- my_ships_total(Current), previous_my_ships_total(Previous), Current > Previous.

% Verifica se il numero di navi dell'IA è diminuito
ships_decreased :- my_ships_total(Current), previous_my_ships_total(Previous), Current < Previous.

% Definizione delle strategie orientate al nemico
enemy_oriented_strategy(aggressive).
enemy_oriented_strategy(co-aggressive).
enemy_oriented_strategy(defensive).

% Definizione delle strategie globali
global_strategy(expansion).

% Unione delle strategie
strategy(S) :- enemy_oriented_strategy(S).
strategy(S) :- global_strategy(S).

