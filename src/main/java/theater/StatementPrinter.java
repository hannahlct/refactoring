package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {

    private final Invoice invoice;
    private final Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        final StringBuilder result = new StringBuilder();
        result.append(String.format("Statement for %s%n", invoice.getCustomer()));

        for (Performance performance : invoice.getPerformances()) {
            final Play play = playFor(performance);

            result.append(String.format(
                    "  %s: %s (%s seats)%n",
                    play.getName(),
                    usd(amountFor(performance)),
                    performance.getAudience()
            ));
        }
        result.append(String.format("Amount owed is %s%n", usd(totalAmount())));
        result.append(String.format("You earned %s credits%n", totalVolumeCredits()));
        return result.toString();
    }

    private Play playFor(Performance performance) {
        return plays.get(performance.getPlayID());
    }

    private int amountFor(Performance performance) {
        final Play play = playFor(performance);
        int result;

        switch (play.getType()) {
            case Constants.TRAGEDY:
                result = Constants.TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;
            case Constants.COMEDY:
                result = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;
            case Constants.HISTORY:
                result = Constants.HISTORY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.HISTORY_AUDIENCE_THRESHOLD) {
                    result += Constants.HISTORY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience()
                            - Constants.HISTORY_AUDIENCE_THRESHOLD);
                }
                break;

            case Constants.PASTORAL:
                result = Constants.PASTORAL_BASE_AMOUNT;
                if (performance.getAudience() > Constants.PASTORAL_AUDIENCE_THRESHOLD) {
                    result += Constants.PASTORAL_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience()
                            - Constants.PASTORAL_AUDIENCE_THRESHOLD);
                }
                break;
            default:
                throw new IllegalArgumentException("unknown type: " + play.getType());
        }

        return result;
    }

    private int volumeCreditsFor(Performance performance) {
        final String type = playFor(performance).getType();
        int result;

        if (Constants.HISTORY.equals(type)) {
            result = Math.max(performance.getAudience() - Constants.HISTORY_VOLUME_CREDIT_THRESHOLD, 0);
        }
        else if (Constants.PASTORAL.equals(type)) {
            result = Math.max(performance.getAudience() - Constants.PASTORAL_VOLUME_CREDIT_THRESHOLD, 0)
                    + performance.getAudience() / 2;
        }
        else {
            // existing behavior for tragedy/comedy
            result = Math.max(performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);

            if (Constants.COMEDY.equals(type)) {
                result += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
            }
        }
        return result;
    }

    private int totalVolumeCredits() {
        int volumeCredits = 0;

        for (Performance performance : invoice.getPerformances()) {
            volumeCredits += volumeCreditsFor(performance);
        }

        return volumeCredits;
    }

    private int totalAmount() {
        int totalAmount = 0;

        for (Performance performance : invoice.getPerformances()) {
            totalAmount += amountFor(performance);
        }

        return totalAmount;
    }

    private String usd(int amountInCents) {
        final NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        return format.format(amountInCents / (double) Constants.PERCENT_FACTOR);
    }
}
