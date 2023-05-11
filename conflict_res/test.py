def is_head_on_collision(reservation_table, next_vertex, current_time, agent):
    next_time = current_time + 1
    # Check if proposed vertex is in the reservation table
    if (next_vertex, next_time) in reservation_table:
        # Head on collision case where agent is moving to a vertex which another agent is coming from 
        if ((next_vertex, next_time - 1) in reservation_table and
            (reservation_table[(next_vertex, next_time)] != agent or
            reservation_table[(next_vertex, next_time - 1)] != agent)):
            return True
        # If the vertex at next time step is allocated to a different agent, it's a collision
        if reservation_table[(next_vertex, next_time)] != agent:
            return True
    return False

# Test the function
reservation_table = {((1, 1), 0): 0, ((1, 2), 1): 0, ((1, 3), 2): 0, ((1, 4), 3): 0, ((1, 5), 4): 0, ((1, 6), 5): 0}
print(is_head_on_collision(reservation_table, (1, 6), 4, 1))  # True, head-on collision
print(is_head_on_collision(reservation_table, (1, 7), 4, 1))  # False
