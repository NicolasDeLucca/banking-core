package com.bankingcore.shared.paging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageRequestTest {

    @Test
    void keepsAValidPageAndSizeAsGiven() {
        PageRequest request = new PageRequest(2, 10);

        assertThat(request.page()).isEqualTo(2);
        assertThat(request.size()).isEqualTo(10);
    }

    @Test
    void clampsANegativePageToZero() {
        PageRequest request = new PageRequest(-5, 10);

        assertThat(request.page()).isZero();
    }

    @Test
    void fallsBackToTheDefaultSizeWhenSizeIsZeroOrNegative() {
        assertThat(new PageRequest(0, 0).size()).isEqualTo(PageRequest.DEFAULT_SIZE);
        assertThat(new PageRequest(0, -1).size()).isEqualTo(PageRequest.DEFAULT_SIZE);
    }

    @Test
    void clampsAnOversizedSizeToTheMax() {
        // This is the actual security-relevant behavior: a client can't force
        // an unbounded response back open by just asking for a huge page.
        PageRequest request = new PageRequest(0, 1_000_000);

        assertThat(request.size()).isEqualTo(PageRequest.MAX_SIZE);
    }

    @Test
    void ofAppliesDefaultsForNullPageAndSize() {
        PageRequest request = PageRequest.of(null, null);

        assertThat(request.page()).isZero();
        assertThat(request.size()).isEqualTo(PageRequest.DEFAULT_SIZE);
    }

    @Test
    void ofPassesThroughNonNullValues() {
        PageRequest request = PageRequest.of(3, 50);

        assertThat(request.page()).isEqualTo(3);
        assertThat(request.size()).isEqualTo(50);
    }
}
