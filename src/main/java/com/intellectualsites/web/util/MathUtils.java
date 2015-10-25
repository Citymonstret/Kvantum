//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2015 IntellectualSites                                                                  /
//                                                                                                           /
//     This program is free software; you can redistribute it and/or modify                                  /
//     it under the terms of the GNU General Public License as published by                                  /
//     the Free Software Foundation; either version 2 of the License, or                                     /
//     (at your option) any later version.                                                                   /
//                                                                                                           /
//     This program is distributed in the hope that it will be useful,                                       /
//     but WITHOUT ANY WARRANTY; without even the implied warranty of                                        /
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         /
//     GNU General Public License for more details.                                                          /
//                                                                                                           /
//     You should have received a copy of the GNU General Public License along                               /
//     with this program; if not, write to the Free Software Foundation, Inc.,                               /
//     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.                                           /
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualsites.web.util;

public class MathUtils {

    public static class Indices {

        public static int index(int base, int index) {
            if (index == 0) {
                return indexZero(base);
            } else if (index == 1) {
                return indexOne(base);
            } else {
                return index(base, base, index);
            }
        }

        private static int index(int base, int current, int count) {
            if (count == 1) {
                return current;
            } else {
                return index(base, current * base, --count);
            }
        }

        public static int indexOne(int base) {
            return base;
        }

        public static int indexZero(int base) {
            return 1;
        }

    }

}
