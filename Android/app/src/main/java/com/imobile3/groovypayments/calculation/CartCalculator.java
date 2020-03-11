package com.imobile3.groovypayments.calculation;

import com.imobile3.groovypayments.data.entities.CartProductEntity;
import com.imobile3.groovypayments.data.entities.CartTaxEntity;
import com.imobile3.groovypayments.data.model.Cart;
import com.imobile3.groovypayments.logging.LogHelper;

import androidx.annotation.NonNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class CartCalculator {

    private final String TAG = CartCalculator.class.getSimpleName();

    @NonNull
    private Cart mCart;

    public CartCalculator(@NonNull Cart cart) {
        mCart = Objects.requireNonNull(cart);
    }

    public void calculate() {
        long sumProductTotal = getSumProductTotal();
        LogHelper.write(Level.FINE, TAG, "Sum(ProductTotal) = " + sumProductTotal);

        long sumTaxTotal = getSumTaxTotal(sumProductTotal);
        LogHelper.write(Level.FINE, TAG, "Sum(TaxTotal) = " + sumTaxTotal);

        mCart.setSubtotal(sumProductTotal);
        mCart.setTaxTotal(sumTaxTotal);

        long grandTotal = sumProductTotal + sumTaxTotal;
        LogHelper.write(Level.FINE, TAG, "GrandTotal = " + grandTotal);

        mCart.setGrandTotal(grandTotal);
    }

    private long getSumProductTotal() {
        long sumProductTotal = 0L;

        List<CartProductEntity> products = mCart.getProducts();
        for (CartProductEntity product : products) {
            // Price of a product is UnitPrice * Quantity
            long productTotal = product.getUnitPrice() * product.getQuantity();
            sumProductTotal += productTotal;
        }

        return sumProductTotal;
    }

    // Note: For this current implementation, we are applying all taxes to all products
    // within the Cart. In a real-world implementation, taxes would only be applied
    // to the products which they are associate with. An example might be a Food Tax
    // that is only applied to Food items, or a Liquor Tax that is only applied to Liquor.
    private long getSumTaxTotal(long productTotal) {
        long sumTaxTotal = 0L;

        List<CartTaxEntity> taxes = mCart.getTaxes();
        for (CartTaxEntity tax : taxes) {
            BigDecimal rate = tax.getRate();
            // This gives us how many tax pennies with decimal precision
            // Example: 8.94 is 8 pennies and 94 hundredths of a penny.
            BigDecimal taxPennies = new BigDecimal(productTotal).multiply(rate);
            // Round down or up to the nearest penny.
            taxPennies = taxPennies.setScale(0, BigDecimal.ROUND_HALF_UP);
            sumTaxTotal += taxPennies.longValue();
        }

        return sumTaxTotal;
    }
}