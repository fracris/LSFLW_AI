% ===== FATTI BASE E IDENTIFICAZIONE SISTEMI =====
% Identifica i sistemi controllati dall'IA
my_system(S) :- system(S), owner(S, P), ai_player(P).

% Identifica i giocatori nemici
enemy(P) :- system(S), owner(S, P), not ai_player(P), P!=0.

% Identifica i sistemi nemici
enemy_system(S, P) :- system(S), owner(S, P), not ai_player(P), P!=0.

% Connessioni non orientate tra sistemi
undirected_connected(M, S) :- connected(M, S).
undirected_connected(M, S) :- connected(S, M).

% ===== ANALISI TATTICHE =====
% Identifica i sistemi di confine
border_system(S) :- my_system(S), undirected_connected(S, N), not my_system(N).

% Sistemi di confine specifici per nemici e neutrali
border_to_enemy(S, P) :- my_system(S), undirected_connected(S, N), enemy_system(N, P).
border_to_neutral(S) :- my_system(S), undirected_connected(S, N), neutral_system(N).

% Identifica i sistemi ad alta produzione
high_production_system(S) :- production(S, P), P > 2.

% ===== CONTEGGI E METRICHE =====
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

% ===== ANALISI SITUAZIONE STORICA =====
% Determina se un nemico si è rafforzato
enemy_strengthened(P) :- enemy(P), enemy_ships_total(P, Current), previous_enemy_ships_total(P, Previous), Current > Previous.

% Verifica perdite significative (2 o più sistemi)
significant_losses :- #count{S : system_lost(S)} >= 2.

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

% ===== CONDIZIONI STRATEGICHE =====
% Condizioni per strategia di espansione
early_game :- my_system_count(C), C <= 3.
expansion_conditions :- early_game, neutral_opportunity.

% Condizioni per strategia difensiva
defensive_conditions :- systems_lost.
urgent_defensive_conditions :- significant_losses.

% Condizioni per attacco diretto
weak_enemy(P) :- enemy(P), my_ships_total(My), enemy_ships_total(P, Enemy), My > Enemy + 20.
direct_attack_conditions(P) :- weak_enemy(P), difficulty(medium).

% Condizioni per attacco cooperativo
cooperative_target(Enemy_S, P) :- enemy_system(Enemy_S, P), #count{My_S : border_to_enemy(My_S, P), undirected_connected(My_S, Enemy_S)} >= 2.
cooperative_attack_conditions(P) :- cooperative_target(_, P), difficulty(hard).

% ===== DEFINIZIONE STRATEGIE =====
% Strategia globale
global_strategy(expansion).

% Strategie orientate al nemico
enemy_oriented_strategy(defensive).
enemy_oriented_strategy(direct_attack).
enemy_oriented_strategy(cooperative_attack).

% Unione delle strategie
strategy(S) :- enemy_oriented_strategy(S).
strategy(S) :- global_strategy(S).

% ===== APPLICABILITÀ STRATEGIE =====
% Strategia di espansione applicabile
applicable_strategy(expansion) :- expansion_conditions.

% Strategia difensiva applicabile
applicable_strategy(defensive) :- defensive_conditions.

% Strategia di attacco diretto applicabile
applicable_strategy(direct_attack) :- direct_attack_conditions(_).

% Strategia di attacco cooperativo applicabile
applicable_strategy(cooperative_attack) :- cooperative_attack_conditions(_).

% ===== SELEZIONE STRATEGIE =====
% Regole per la selezione delle strategie in base al livello di difficoltà
{ chosen_strategy(S) : applicable_strategy(S) } = 1 :- difficulty(medium).
{ chosen_strategy(S) : applicable_strategy(S) } = 2 :- difficulty(hard).

% Per livello easy, seleziona solo una strategia semplice
{ chosen_strategy(S) : applicable_strategy(S) } = 1 :- difficulty(easy).

% ===== WEAK CONSTRAINTS PER PRIORITÀ =====
% Priorità massima per difesa urgente
:~ urgent_defensive_conditions, not chosen_strategy(defensive). [100@5]

% Alta priorità per strategia difensiva quando si perdono sistemi
:~ systems_lost, not chosen_strategy(defensive). [80@4]

% Priorità per attacco cooperativo su livello hard
:~ difficulty(hard), cooperative_attack_conditions(_), not chosen_strategy(cooperative_attack). [70@3]

% Priorità per attacco diretto quando nemico è debole
:~ weak_enemy(P), not chosen_strategy(direct_attack). [60@3]

% Priorità per espansione nei primi turni
:~ early_game, neutral_opportunity, not chosen_strategy(expansion). [50@2]

% Evita strategie non applicabili
:~ chosen_strategy(S), not applicable_strategy(S). [1000@6]

% ===== IMPLEMENTAZIONE AZIONI STRATEGICHE =====
% ESPANSIONE: attacca sistemi neutrali vicini
target_neutral(From, To) :- chosen_strategy(expansion), my_system(From), neutral_system(To), undirected_connected(From, To), ships(From, Ships), Ships > 5.

% DIFENSIVA: rinforza sistemi di confine
reinforce_border(From, To) :- chosen_strategy(defensive), my_system(From), my_system(To), border_system(To), undirected_connected(From, To), ships(From, FromShips), ships(To, ToShips), FromShips > ToShips + 3, FromShips > 5.

% ATTACCO DIRETTO: attacca nemico debole
direct_attack(From, To) :- chosen_strategy(direct_attack), my_system(From), enemy_system(To, P), weak_enemy(P), undirected_connected(From, To), ships(From, FromShips), ships(To, ToShips), FromShips > ToShips + 2.

% ATTACCO COOPERATIVO: coordina attacchi multipli
cooperative_attack(From, To) :- chosen_strategy(cooperative_attack), my_system(From), enemy_system(To, P), cooperative_target(To, P), undirected_connected(From, To), ships(From, Ships), Ships > 3.

% ===== GENERAZIONE AZIONI SEND_FLEET =====
% Calcola le navi da inviare per ogni tipo di azione
expansion_ships(From, Ships) :- target_neutral(From, _), ships(From, TotalShips), Ships = (TotalShips * 3) / 4.
defensive_ships(From, Ships) :- reinforce_border(From, _), ships(From, TotalShips), Ships = (TotalShips * 1) / 2.
attack_ships(From, Ships) :- direct_attack(From, To), ships(From, FromShips), ships(To, ToShips), Ships = ToShips + 5, Ships <= FromShips - 1.
cooperative_ships(From, Ships) :- cooperative_attack(From, _), ships(From, TotalShips), Ships = (TotalShips * 2) / 3.

% ===== AZIONI FINALI =====
% Genera le azioni send_fleet per espansione
send_fleet(From, To, Ships) :- target_neutral(From, To), expansion_ships(From, Ships), Ships > 0.

% Genera le azioni send_fleet per difesa
send_fleet(From, To, Ships) :- reinforce_border(From, To), defensive_ships(From, Ships), Ships > 0.

% Genera le azioni send_fleet per attacco diretto
send_fleet(From, To, Ships) :- direct_attack(From, To), attack_ships(From, Ships), Ships > 0.

% Genera le azioni send_fleet per attacco cooperativo
send_fleet(From, To, Ships) :- cooperative_attack(From, To), cooperative_ships(From, Ships), Ships > 0.

% ===== OTTIMIZZAZIONI =====
% Limita il numero di azioni per livello
:- difficulty(medium), #count{From, To, Ships : send_fleet(From, To, Ships)} > 1.
:- difficulty(hard), #count{From, To, Ships : send_fleet(From, To, Ships)} > 2.
:- difficulty(easy), #count{From, To, Ships : send_fleet(From, To, Ships)} > 1.

% Evita azioni che lascerebbero sistemi vulnerabili
:- send_fleet(From, _, Ships), ships(From, Total), Ships >= Total.

% Preferisci azioni che utilizzano sistemi con più navi
:~ send_fleet(From, _, _), ships(From, Ships). [10 - Ships@1]

#show send_fleet/3.
#show chosen_strategy/1.