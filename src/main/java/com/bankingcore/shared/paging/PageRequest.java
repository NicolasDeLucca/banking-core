package com.bankingcore.shared.paging;

/**
 * A zero-based page number and a clamped page size, shared by every paginated
 * repository query across modules. Deliberately its own type (not Spring
 * Data's Pageable) so domain repository interfaces stay free of any
 * framework dependency, matching the rest of this codebase's "Option B" pure
 * domain layer.
 */
public record PageRequest(int page, int size) {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    // NOPMD-wide note: PMD 7's UnusedAssignment rule doesn't recognize that
    // reassigning a record compact constructor's implicit parameter is what
    // sets the field - it's the idiomatic way to validate/normalize a
    // record's state, not a dead store.
    public PageRequest {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = DEFAULT_SIZE; // NOPMD - UnusedAssignment: reassigns the compact ctor's implicit field
        } else if (size > MAX_SIZE) {
            // A caller asking for an unreasonably large page is exactly the
            // unbounded-response problem this type exists to prevent, so this
            // clamps rather than rejects - the same policy a bare "size" query
            // param would otherwise need enforced separately in every controller.
            size = MAX_SIZE; // NOPMD - UnusedAssignment: reassigns the compact ctor's implicit field
        }
    }

    /** Builds from nullable request params (e.g. optional @RequestParam Integer), applying defaults. */
    public static PageRequest of(Integer page, Integer size) {
        return new PageRequest(page == null ? 0 : page, size == null ? DEFAULT_SIZE : size);
    }
}
