
% Identifica i sistemi controllati dall'IA
my_system(S) :- system(S), owner(S, P), ai_player(P).


% Identifica i giocatori nemici
enemy(P) :- system(S), owner(S, P), not ai_player(P), P != 0.

% Identifica i sistemi nemici
enemy_system(S, P) :- system(S), owner(S, P), not ai_player(P), P != 0.

% Identifica i sistemi neutrali
neutral_system(S) :- system(S), neutral(S).

% Connessioni non orientate tra sistemi
undirected_connected(M, S) :- connected(M, S).
undirected_connected(M, S) :- connected(S, M).

% Identifica i sistemi di confine
border_system(S) :- my_system(S), undirected_connected(S, N), not my_system(N).

% Identifica i sistemi ad alta produzione
high_production_system(S) :- system(S), production(S, P), P >= 2.

% Conta i sistemi controllati dall'IA
% count_my_systems(S) :- my_system(S).
my_system_count(C) :- #count{S : my_system(S) } = C.

% Conta i sistemi nemici per ogni giocatore nemico
% count_enemy_systems(S, P) :- enemy_system(S, P).
enemy_system_count(P, C) :- enemy(P), #count{S : enemy_system(S, P)} = C.

% Conta i sistemi neutrali
count_neutral_systems(S) :- neutral_system(S).
neutral_system_count(C) :- #count{S : count_neutral_systems(S)} = C.

% Somma delle navi nei sistemi controllati dall'IA
sum_my_ships(Ships, S) :- my_system(S), ships(S, Ships).
my_ships_total(T) :- #sum{Ships, S : sum_my_ships(Ships, S)} = T.

% Somma delle navi nei sistemi nemici per ogni giocatore nemico
sum_enemy_ships(Ships, S, P) :- enemy_system(S, P), ships(S, Ships).
enemy_ships_total(P, T) :- enemy(P), #sum{Ships, S : sum_enemy_ships(Ships, S, P)} = T.

% Determina se l'IA è in inferiorità numerica rispetto a un nemico
outnumbered(P) :- enemy(P), my_ships_total(MyTotal), enemy_ships_total(P, EnemyTotal), MyTotal < EnemyTotal.

% Determina se l'IA è superiore rispetto a un nemico
superior(P) :- enemy(P), my_ships_total(MyTotal), enemy_ships_total(P, EnemyTotal), MyTotal * 10 > EnemyTotal * 13.

% Determina se un nemico si è indebolito
enemy_weakened(P) :- enemy(P), enemy_ships_total(P, Current), previous_enemy_ships_total(P, Previous), Current < Previous.

% Determina se un nemico si è rafforzato
enemy_strengthened(P) :- enemy(P), enemy_ships_total(P, Current), previous_enemy_ships_total(P, Previous), Current > Previous.

% Opportunità di espansione in presenza di sistemi neutrali
neutral_opportunity :- neutral_system_count(Count), Count > 0.

% Verifica se l'IA ha guadagnato sistemi
systems_gained :- my_system_count(Current), previous_my_system_count(Previous), Current > Previous.

% Verifica se l'IA ha perso sistemi
systems_lost :- my_system_count(Current), previous_my_system_count(Previous), Current < Previous.

% Verifica se il numero di navi dell'IA è aumentato
ships_increased :- my_ships_total(Current), previous_my_ships_total(Previous), Current > Previous.

% Verifica se il numero di navi dell'IA è diminuito
ships_decreased :- my_ships_total(Current), previous_my_ships_total(Previous), Current < Previous.

% Definizione delle strategie orientate al nemico
enemy_oriented_strategy(aggressive).
enemy_oriented_strategy(defensive).

% Definizione delle strategie globali
global_strategy(expansion).
global_strategy(consolidation).

% Unione delle strategie
strategy(S) :- enemy_oriented_strategy(S).
strategy(S) :- global_strategy(S).

% Candidati per strategia aggressiva
candidate_aggressive(P) :- superior(P), enemy_weakened(P).
candidate_aggressive(P) :- superior(P), systems_gained.

% Candidati per strategia difensiva
candidate_defensive(P) :- enemy(P), outnumbered(P), not superior(P), my_ships_total(MT), enemy_ships_total(P, ET), MT * 10 > ET * 7.
candidate_defensive(P) :- enemy(P), enemy_strengthened(P), not superior(P), not systems_lost.

% CORREZIONE: Logica di espansione più forte
% Espansione quando ci sono molti sistemi neutrali rispetto ai nostri
strong_expansion_opportunity :- neutral_system_count(NC), my_system_count(MC), NC >= MC * 5.
% Espansione quando abbiamo pochi sistemi e molte navi
expansion_ready :- my_system_count(MC), MC <= 3, my_ships_total(ST), ST >= 200.

candidate_expansion :- neutral_opportunity, my_system_count(MC), MC <= 2.
candidate_expansion :- strong_expansion_opportunity.
candidate_expansion :- expansion_ready, neutral_opportunity.
candidate_expansion :- ships_increased, neutral_opportunity, not systems_lost.

% CORREZIONE: Consolidamento molto più restrittivo
% Solo quando abbiamo molti sistemi E ci sono minacce immediate
immediate_threat :- enemy(P), enemy_ships_total(P, ET), my_ships_total(MT), ET * 10 > MT * 8.
many_systems :- my_system_count(MC), MC >= 4.

candidate_consolidation :- many_systems, immediate_threat.
candidate_consolidation :- systems_gained, my_system_count(Count), Count >= 4, border_system(S), enemy(P).

% Calcolo del peso per la strategia aggressiva
count_aggressive(P) :- candidate_aggressive(P).
strategy_weight(aggressive, W) :- #count{P : count_aggressive(P)} = W.

% Calcolo del peso per la strategia difensiva
count_defensive(P) :- candidate_defensive(P).
strategy_weight(defensive, W) :- #count{P : count_defensive(P)} = W.

% CORREZIONE: Pesi molto più forti per l'espansione
strategy_weight(expansion, 5) :- strong_expansion_opportunity.
strategy_weight(expansion, 4) :- my_system_count(MC), MC <= 2, neutral_opportunity.
strategy_weight(expansion, 3) :- expansion_ready.
strategy_weight(expansion, 2) :- candidate_expansion, my_system_count(MC), MC <= 3.
strategy_weight(expansion, 1) :- candidate_expansion.
strategy_weight(expansion, 0) :- not candidate_expansion.

% CORREZIONE: Peso molto basso per consolidamento
strategy_weight(consolidation, 1) :- candidate_consolidation, many_systems.
strategy_weight(consolidation, 0) :- not candidate_consolidation.
strategy_weight(consolidation, 0) :- my_system_count(MC), MC <= 3.

% CORREZIONE: Vincolo di cardinalità SEMPRE attivo (non solo per medium)
{ chosen_strategy(S) : strategy(S) } = 1.

% Selezione aggiuntiva per livello difficile
{ chosen_strategy(S) : enemy_oriented_strategy(S) } = 1 :- difficulty(hard).
{ chosen_strategy(S) : global_strategy(S) } = 1 :- difficulty(hard).

% CORREZIONE: Ottimizzazione per massimizzare i pesi (segno negativo)
:~ chosen_strategy(S), strategy_weight(S, W). [-W@3]

% CORREZIONE: Penalizzazioni molto forti per scelte inappropriate
:~ chosen_strategy(consolidation), my_system_count(MC), MC <= 3. [20@4]
:~ chosen_strategy(consolidation), strong_expansion_opportunity. [15@4]
:~ chosen_strategy(consolidation), neutral_system_count(NC), my_system_count(MC), NC >= MC * 3. [10@4]

% Bonus per scelte appropriate
:~ chosen_strategy(expansion), strong_expansion_opportunity. [-5@2]
:~ chosen_strategy(expansion), my_system_count(MC), MC <= 2, neutral_opportunity. [-3@2]
:~ chosen_strategy(aggressive), superior(P). [-2@2]

% Priorità addizionali per situazioni specifiche
:~ chosen_strategy(defensive), systems_lost. [-3@1]
:~ chosen_strategy(expansion), neutral_opportunity. [-1@1]

#show chosen_strategy/1.
#show my_system_count/1.
#show enemy_system_count/2.
#show neutral_system_count/1.
#show my_ships_total/1.
#show enemy_ships_total/2.
#show strong_expansion_opportunity/0.
#show candidate_expansion/0.
#show candidate_consolidation/0.
#show system_lost/1.